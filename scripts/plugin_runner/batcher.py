# TODO: docs

import logging
from abc import ABC, abstractmethod
from enum import Enum, unique
from pathlib import Path
from typing import Any, Dict, List, Set, Tuple, Type

from binpackp.bins import NumberBin
from binpackp.fit import BPResult, BinReduction, Fit

logger = logging.getLogger(__name__)


class Batcher(ABC):
    @classmethod
    @abstractmethod
    def split_into_batches(
        cls,
        projects: Dict[Path, Dict[str, int]],
        batch_constraints: Dict[str, int],
        ignore_oversized_projects: bool = True,
        **kwargs,
    ) -> List[List[Path]]:
        raise NotImplementedError


@unique
class BatcherArgument(Enum):
    BATCH_SIZE = 'batch_size'
    MAX_OPEN_BATCHES = 'max_open_batches'


class DummyBatcher(Batcher):
    @classmethod
    def split_into_batches(
        cls,
        projects: Dict[Path, Dict[str, int]],
        batch_constraints: Dict[str, int],
        ignore_oversized_projects: bool = True,
        **kwargs,
    ) -> List[List[Path]]:
        if batch_constraints:
            logger.warning('Batch constraints will be ignored.')

        batch_size = kwargs.get(BatcherArgument.BATCH_SIZE.value, 50)
        return [list(projects.keys())[i : i + batch_size] for i in range(0, len(projects), batch_size)]


class OneDimensionalAnyFitBatcher(Batcher):
    @classmethod
    @abstractmethod
    def heuristic_split(
        cls,
        projects: List[Tuple[Path, int]],
        batch_constraint: int,
        **kwargs,
    ) -> BPResult:
        raise NotImplementedError

    @classmethod
    def split_into_batches(
        cls,
        projects: Dict[Path, Dict[str, int]],
        batch_constraints: Dict[str, int],
        ignore_oversized_projects: bool = True,
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
    @classmethod
    def heuristic_split(cls, projects: List[Tuple[Path, int]], batch_constraint: int, **kwargs) -> BPResult:
        return Fit.ffd(NamedNumberBin, batch_constraint, projects)


class OneDimensionalBestFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    @classmethod
    def heuristic_split(cls, projects: List[Tuple[Path, int]], batch_constraint: int, **kwargs) -> BPResult:
        return Fit.bfd(NamedNumberBin, batch_constraint, projects)


class OneDimensionalWorstFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    @classmethod
    def heuristic_split(cls, projects: List[Tuple[Path, int]], batch_constraint: int, **kwargs) -> BPResult:
        return Fit.wfd(NamedNumberBin, batch_constraint, projects)


class OneDimensionalNextFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
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
        ignore_oversized_batches: bool = True,
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
