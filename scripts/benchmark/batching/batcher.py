# TODO: docs

import logging
from abc import ABC, abstractmethod
from typing import Dict, List, Tuple

from binpackp.bins import NumberBin
from binpackp.fit import BPResult, BinReduction, Fit

from benchmark.batching.config import ConfigField
from benchmark.metrics_collection.metrics import MetricName

logger = logging.getLogger(__name__)


class Batcher(ABC):
    @abstractmethod
    def split_into_batches(
        self,
        projects: Dict[str, Dict[MetricName, int]],
        metric_constraints: Dict[MetricName, int],
        ignore_oversized_projects: bool = True,
        **kwargs,
    ) -> List[List[str]]:
        raise NotImplementedError


class OneDimensionalAnyFitBatcher(Batcher):
    @abstractmethod
    def heuristic_split(
        self,
        projects: List[Tuple[str, int]],
        bin_constraint: int,
        **kwargs,
    ) -> BPResult:
        raise NotImplementedError

    def split_into_batches(
        self,
        projects: Dict[str, Dict[MetricName, int]],
        metric_constraints: Dict[MetricName, int],
        ignore_oversized_projects: bool = True,
        **kwargs,
    ) -> List[List[str]]:
        if len(metric_constraints) != 1:
            raise ValueError('Multiple metric constraints are not supported.')

        if not projects:
            return []

        metric_name, metric_constraint = next(iter(metric_constraints.items()))

        projects_for_batching = [
            (project, metrics[metric_name])
            for project, metrics in projects.items()
            if metrics.get(metric_name) is not None
        ]

        if len(projects) != len(projects_for_batching):
            logger.warning(f"Projects without the '{metric_name.value}' metric will be skipped.")

        oversized_bins = [
            [project] for project, metric_value in projects_for_batching if metric_value > metric_constraint
        ]

        projects_for_batching = [
            (project, metric_value)
            for project, metric_value in projects_for_batching
            if metric_value <= metric_constraint
        ]

        if oversized_bins:
            if ignore_oversized_projects:
                logger.warning(f'{len(oversized_bins)} oversized projects will be ignored.')
                oversized_bins.clear()
            else:
                logger.warning(f'{len(oversized_bins)} oversized projects will be placed in separate bins.')

        heuristic_result = self.heuristic_split(projects_for_batching, metric_constraint, **kwargs)
        return [[project for project, _ in result_bin] for result_bin in heuristic_result.bins] + oversized_bins


class NamedNumberBin(NumberBin):
    @classmethod
    def size(cls, item: Tuple[str, int]):
        _, value = item
        return super().size(value)


class OneDimensionalFirstFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    def heuristic_split(self, projects: List[Tuple[str, int]], bin_constraint: int, **kwargs) -> BPResult:
        return Fit.ffd(NamedNumberBin, bin_constraint, projects)


class OneDimensionalBestFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    def heuristic_split(self, projects: List[Tuple[str, int]], bin_constraint: int, **kwargs) -> BPResult:
        return Fit.bfd(NamedNumberBin, bin_constraint, projects)


class OneDimensionalWorstFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    def heuristic_split(self, projects: List[Tuple[str, int]], bin_constraint: int, **kwargs) -> BPResult:
        return Fit.wfd(NamedNumberBin, bin_constraint, projects)


class OneDimensionalNextFitDecreasingBatcher(OneDimensionalAnyFitBatcher):
    def heuristic_split(self, projects: List[Tuple[str, int]], bin_constraint: int, **kwargs) -> BPResult:
        max_open_bins = kwargs.get(ConfigField.MAX_OPEN_BINS.value, 1)

        return Fit.fit(
            NamedNumberBin,
            bin_constraint,
            projects,
            sort=True,
            max_open_bins=max_open_bins,
            bin_reduction=BinReduction.first,
        )
