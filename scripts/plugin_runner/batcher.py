import logging
from abc import ABC, abstractmethod
from enum import Enum, unique
from pathlib import Path
from typing import Any, Dict, List, Set, Tuple, Type

from binpackp.bins import NumberBin
from binpackp.fit import BPResult, BinReduction, Fit

logger = logging.getLogger(__name__)


IGNORE_OVERSIZED_PROJECTS_DEFAULT = True


class Batcher(ABC):
    """An abstract class for all batchers."""

    @classmethod
    @abstractmethod
    def split_into_batches(
        cls,
        projects: Dict[Path, Dict[str, int]],
        batch_constraints: Dict[str, int],
        ignore_oversized_projects: bool = IGNORE_OVERSIZED_PROJECTS_DEFAULT,
        **kwargs,
    ) -> List[List[Path]]:
        """
        Split projects into batches, using batch constraints.

        :param projects: Dictionary where for each project metrics are specified.
        :param batch_constraints: Dictionary where for each metric batch constraint is specified.
        :param ignore_oversized_projects: Whether to ignore projects that do not fit fully into one batch. By default,
        true.
        :param kwargs: Additional arguments.
        :return: List of batches. Each batch is a list of project paths included in that batch.
        """
        raise NotImplementedError


@unique
class BatcherArgument(Enum):
    BATCH_SIZE = 'batch_size'
    MAX_OPEN_BATCHES = 'max_open_batches'


class DummyBatcher(Batcher):
    """
    A class implementing a simple sequential batching strategy.

    It splits projects into batches using following algorithm:
        1. Projects are grouped into batches of ``n`` projects in the order in which they are passed on.
        2. If there are less than ``n`` projects in the last batch, they make up the final batch of the smaller one.

    It accepts the following additional arguments:
        * batch_size -- Maximum number of projects that can be contained in a batch. By default, 50.
    """

    @classmethod
    def split_into_batches(
        cls,
        projects: Dict[Path, Dict[str, int]],
        batch_constraints: Dict[str, int],
        ignore_oversized_projects: bool = IGNORE_OVERSIZED_PROJECTS_DEFAULT,
        **kwargs,
    ) -> List[List[Path]]:
        """
        Split projects into batches, using a sequential batching strategy.

        :param projects: Dictionary with projects passed on as keys. Dictionary values will be ignored.
        :param batch_constraints: Will be ignored.
        :param ignore_oversized_projects: Will be ignored.
        :param kwargs: Will be ignored.
        :return: List of batches. Each batch is a list of project paths included in that batch.
        """
        if batch_constraints:
            logger.warning('Batch constraints will be ignored.')

        batch_size = kwargs.get(BatcherArgument.BATCH_SIZE.value, 50)
        return [list(projects.keys())[i:(i + batch_size)] for i in range(0, len(projects), batch_size)]


class OneDimensionalAnyFitBatcher(Batcher):
    """An abstract class implementing an 1D Any Fit batching strategy."""

    @classmethod
    @abstractmethod
    def heuristic_split(
        cls,
        projects: List[Tuple[Path, int]],
        batch_constraint: int,
        **kwargs,
    ) -> BPResult:
        """
        Split projects into batches using an any fit heuristic.

        :param projects: List of tuples where for each project metric is specified.
        :param batch_constraint: Batch constraint.
        :param kwargs: Additional arguments.
        :return: Instance of ``BPResult``.
        """
        raise NotImplementedError

    @classmethod
    def split_into_batches(
        cls,
        projects: Dict[Path, Dict[str, int]],
        batch_constraints: Dict[str, int],
        ignore_oversized_projects: bool = IGNORE_OVERSIZED_PROJECTS_DEFAULT,
        **kwargs,
    ) -> List[List[Path]]:
        if len(batch_constraints) != 1:
            raise ValueError('You must pass exactly one batch constraint.')

        if not projects:
            return []

        constraint_name, constraint_value = next(iter(batch_constraints.items()))

        projects_for_batching = [
            (project, metrics[constraint_name])
            for project, metrics in projects.items()
            if metrics.get(constraint_name) is not None
        ]

        if len(projects) != len(projects_for_batching):
            logger.warning(
                f'{len(projects) - len(projects_for_batching)} projects without '
                f"the '{constraint_name}' metric will be skipped.",
            )

        oversized_batches = [
            [project] for project, metric_value in projects_for_batching if metric_value > constraint_value
        ]

        projects_for_batching = [
            (project, metric_value)
            for project, metric_value in projects_for_batching
            if metric_value <= constraint_value
        ]

        if oversized_batches:
            if ignore_oversized_projects:
                logger.warning(f'{len(oversized_batches)} oversized projects will be ignored.')
                oversized_batches.clear()
            else:
                logger.warning(f'{len(oversized_batches)} oversized projects will be placed in separate batches.')

        heuristic_result = cls.heuristic_split(projects_for_batching, constraint_value, **kwargs)

        batches = [
            [project for project, _ in result_bin] for result_bin in heuristic_result.bins if len(result_bin) > 0
        ]

        return batches + oversized_batches


class NamedNumberBin(NumberBin):
    @classmethod
    def size(cls, item: Tuple[Any, int]):
        _, value = item
        return super().size(value)


class OneDimensionalFirstFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    """
    A class implementing an 1D First Fit Decreasing batching strategy.

    It splits projects into batches using following algorithm:
        1. Sort all project metrics in descending order.
        2. Use First Fit algorithm: https://en.wikipedia.org/wiki/First-fit_bin_packing.
    """

    @classmethod
    def heuristic_split(cls, projects: List[Tuple[Path, int]], batch_constraint: int, **kwargs) -> BPResult:
        return Fit.ffd(NamedNumberBin, batch_constraint, projects)


class OneDimensionalBestFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    """
    A class implementing an 1D Best Fit Decreasing batching strategy.

    It splits projects into batches using following algorithm:
        1. Sort all project metrics in descending order.
        2. Use Best Fit algorithm: https://en.wikipedia.org/wiki/Best-fit_bin_packing.
    """

    @classmethod
    def heuristic_split(cls, projects: List[Tuple[Path, int]], batch_constraint: int, **kwargs) -> BPResult:
        return Fit.bfd(NamedNumberBin, batch_constraint, projects)


class OneDimensionalWorstFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    """
    A class implementing an 1D Worst Fit Decreasing batching strategy.

    It splits projects into batches using following algorithm:
        1. Sort all project metrics in descending order.
        2. Use Worst Fit algorithm: https://en.wikipedia.org/wiki/Bin_packing_problem#Online_heuristics.
    """

    @classmethod
    def heuristic_split(cls, projects: List[Tuple[Path, int]], batch_constraint: int, **kwargs) -> BPResult:
        return Fit.wfd(NamedNumberBin, batch_constraint, projects)


class OneDimensionalNextFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    """
    A class implementing an 1D Next Fit Decreasing batching strategy.

    It splits projects into batches using following algorithm:
        1. Sort all project metrics in descending order.
        2. Use Next Fit algorithm: https://en.wikipedia.org/wiki/Next-fit_bin_packing.

    It accepts the following additional arguments:
        * max_open_batches -- Maximum number of open batches. If the value is greater than 1, the Next-k-Fit algorithm
          is used. By default, it equals 1.
    """

    @classmethod
    def heuristic_split(cls, projects: List[Tuple[Path, int]], batch_constraint: int, **kwargs) -> BPResult:
        return Fit.fit(
            NamedNumberBin,
            batch_constraint,
            projects,
            sort=True,
            max_open_bins=kwargs.get(BatcherArgument.MAX_OPEN_BATCHES.value, 1),
            bin_reduction=BinReduction.first,
        )


@unique
class BatcherName(Enum):
    DUMMY_BATCHER = 'dummy_batcher'
    ONE_DIMENSIONAL_FIRST_FIT_DECREASING = '1d_first_fit_decreasing'
    ONE_DIMENSIONAL_BEST_FIT_DECREASING = '1d_best_fit_decreasing'
    ONE_DIMENSIONAL_WORST_FIT_DECREASING = '1d_worst_fit_decreasing'
    ONE_DIMENSIONAL_NEXT_FIT_DECREASING = '1d_next_fit_decreasing'

    def execute(
        self,
        projects: Dict[Path, Dict[str, int]],
        batch_constraints: Dict[str, int],
        ignore_oversized_batches: bool = IGNORE_OVERSIZED_PROJECTS_DEFAULT,
        **kwargs,
    ) -> List[List[Path]]:
        return BATCHER_NAME_TO_CLASS[self].split_into_batches(
            projects,
            batch_constraints,
            ignore_oversized_batches,
            **kwargs,
        )

    @classmethod
    def values(cls) -> Set[str]:
        return {metric.value for metric in cls}


BATCHER_NAME_TO_CLASS: Dict[BatcherName, Type[Batcher]] = {
    BatcherName.DUMMY_BATCHER: DummyBatcher,
    BatcherName.ONE_DIMENSIONAL_FIRST_FIT_DECREASING: OneDimensionalFirstFitDecreasingBatcher,
    BatcherName.ONE_DIMENSIONAL_BEST_FIT_DECREASING: OneDimensionalBestFitDecreasingBatcher,
    BatcherName.ONE_DIMENSIONAL_WORST_FIT_DECREASING: OneDimensionalWorstFitDecreasingBatcher,
    BatcherName.ONE_DIMENSIONAL_NEXT_FIT_DECREASING: OneDimensionalNextFitDecreasingBatcher,
}
