import subprocess
from contextlib import contextmanager
from pathlib import Path
from tempfile import NamedTemporaryFile, TemporaryDirectory
from time import sleep
from typing import Union

import pytest
import selenium.webdriver.support.expected_conditions as ec
from selenium.webdriver import Chrome, Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait

from benchmark.batching.batcher_benchmark import BenchmarkResultColumn
from test.benchmark.batching import RESULTS_COMPARISON_TEST_DATA_FOLDER
from utils.df_utils import AggregationFunction
from utils.file_utils import get_all_file_system_items

PORT = 8501
SCRIPT_PATH = Path(__file__).parents[3] / 'benchmark' / 'batching' / 'results_comparison.py'

IMPLICIT_WAIT = 2
EXPLICIT_WAIT = 30


@contextmanager
def run_streamlit():
    p = subprocess.Popen(
        ['streamlit', 'run', str(SCRIPT_PATH), '--server.port', str(PORT), '--server.headless', 'true'],
    )
    sleep(5)  # Waiting for streamlit to start up

    try:
        yield
    finally:
        p.kill()


@pytest.fixture(scope='module', autouse=True)
def before_module():
    with run_streamlit():
        yield


@pytest.fixture(scope='function', autouse=True)
def before_test(chrome: Chrome):
    chrome.get(f'http://localhost:{PORT}/')
    WebDriverWait(chrome, EXPLICIT_WAIT).until(
        ec.visibility_of_element_located(
            (By.XPATH, '//div[contains(@class, "stSelectbox") and .//*[text() = "Aggregation function:"]]'),
        ),
    )


def fill_input_data(
    chrome: Chrome,
    results: Union[Path, str],
    dataset_size: int = 1000,
    sample_size: int = 100,
    metric: BenchmarkResultColumn = BenchmarkResultColumn.TIME,
    aggregate_function: AggregationFunction = AggregationFunction.MEAN,
):
    assert metric.value in BenchmarkResultColumn.metrics()

    results_path_locator = (By.XPATH, '//input[@aria-label = "Results path:"]')
    chrome.find_element(*results_path_locator).clear()
    chrome.find_element(*results_path_locator).send_keys(str(results))
    chrome.find_element(*results_path_locator).send_keys(Keys.ENTER)
    sleep(IMPLICIT_WAIT)

    dataset_size_locator = (By.XPATH, '//input[@aria-label = "Dataset size:"]')
    chrome.find_element(*dataset_size_locator).clear()
    chrome.find_element(*dataset_size_locator).send_keys(str(dataset_size))
    chrome.find_element(*dataset_size_locator).send_keys(Keys.ENTER)
    sleep(IMPLICIT_WAIT)

    sample_size_locator = (By.XPATH, '//input[@aria-label = "Sample size:"]')
    chrome.find_element(*sample_size_locator).clear()
    chrome.find_element(*sample_size_locator).send_keys(str(sample_size))
    chrome.find_element(*sample_size_locator).send_keys(Keys.ENTER)
    sleep(IMPLICIT_WAIT)

    chrome.find_element(By.XPATH, '//div[contains(@class, "stSelectbox") and .//*[text() = "Metric:"]]').click()
    chrome.find_element(By.XPATH, f'//div[text() = "{metric.value}"]').click()
    sleep(IMPLICIT_WAIT)

    chrome.find_element(
        By.XPATH,
        '//div[contains(@class, "stSelectbox") and .//*[text() = "Aggregation function:"]]',
    ).click()
    chrome.find_element(By.XPATH, f'//div[text() = "{aggregate_function.value}"]').click()
    sleep(IMPLICIT_WAIT)

    # Removing focus from input fields
    chrome.find_element(By.XPATH, '//div[@class = "stMarkdown"]').click()


def test_results_path_does_not_exist(chrome: Chrome):
    fill_input_data(chrome, results='this/path/does/not/exist')

    locator = (By.XPATH, '//div[@class = "stAlert"]')
    WebDriverWait(chrome, EXPLICIT_WAIT).until(ec.visibility_of_element_located(locator))

    assert chrome.find_element(*locator).text == 'The this/path/does/not/exist does not exist.'


def test_results_path_is_not_directory(chrome: Chrome):
    with NamedTemporaryFile() as temp_file:
        fill_input_data(chrome, results=temp_file.name)

        locator = (By.XPATH, '//div[@class = "stAlert"]')
        WebDriverWait(chrome, EXPLICIT_WAIT).until(ec.visibility_of_element_located(locator))

        assert chrome.find_element(*locator).text == f'The {temp_file.name} is not a directory.'


def test_results_path_is_empty(chrome: Chrome):
    with TemporaryDirectory() as temp_dir:
        fill_input_data(chrome, results=temp_dir)

        locator = (By.XPATH, '//div[@class = "stAlert"]')
        WebDriverWait(chrome, EXPLICIT_WAIT).until(ec.visibility_of_element_located(locator))

        assert chrome.find_element(*locator).text == f'The {temp_dir} is empty.'


RESULTS_COMPARED_TEST_DATA = [
    ('one_results_file', 1000, 100, BenchmarkResultColumn.TIME, AggregationFunction.MEAN),
    ('one_results_file', 1000, 100, BenchmarkResultColumn.TIME, AggregationFunction.MAX),
    ('one_results_file', 1000, 100, BenchmarkResultColumn.TIME, AggregationFunction.MEDIAN),
    ('one_results_file', 1000, 100, BenchmarkResultColumn.TIME, AggregationFunction.MIN),
    ('one_results_file', 1000, 100, BenchmarkResultColumn.RSS, AggregationFunction.MEAN),
    ('one_results_file', 1000, 100, BenchmarkResultColumn.RSS, AggregationFunction.MAX),
    ('one_results_file', 1000, 100, BenchmarkResultColumn.RSS, AggregationFunction.MEDIAN),
    ('one_results_file', 1000, 100, BenchmarkResultColumn.RSS, AggregationFunction.MIN),
    ('one_results_file', 1000, 1000, BenchmarkResultColumn.TIME, AggregationFunction.MEAN),
    ('one_results_file_warmup_runs_only', 1000, 100, BenchmarkResultColumn.TIME, AggregationFunction.MEAN),
    ('one_results_file_benchmark_runs_only', 1000, 100, BenchmarkResultColumn.TIME, AggregationFunction.MEAN),
    ('several_results_files', 1000, 100, BenchmarkResultColumn.TIME, AggregationFunction.MEAN),
]


@pytest.mark.parametrize(
    ('results_dir_name', 'dataset_size', 'sample_size', 'metric', 'aggregate_function'),
    RESULTS_COMPARED_TEST_DATA,
)
def test_results_compared(
    chrome: Chrome,
    results_dir_name: str,
    dataset_size: int,
    sample_size: int,
    metric: BenchmarkResultColumn,
    aggregate_function: AggregationFunction,
):
    results_dir = RESULTS_COMPARISON_TEST_DATA_FOLDER / results_dir_name

    fill_input_data(chrome, results_dir, dataset_size, sample_size, metric, aggregate_function)

    chrome.save_screenshot('/home/girz0n/Desktop/screenshot.png')

    comparison_table_locator = (By.XPATH, '//div[contains(@class, "stDataFrame")]')
    WebDriverWait(chrome, EXPLICIT_WAIT).until(ec.visibility_of_element_located(comparison_table_locator))
    assert len(chrome.find_elements(*comparison_table_locator)) == 1
    assert chrome.find_element(*comparison_table_locator).is_displayed()

    for results_path in get_all_file_system_items(results_dir, with_subdirs=False):
        chrome.find_element(
            By.XPATH,
            '//div[contains(@class, "stSelectbox") and .//*[text() = "Result name:"]]',
        ).click()
        chrome.find_element(By.XPATH, f'//div[text() = "{results_path.stem}"]').click()

        plotly_chart_locator = (By.XPATH, '//div[contains(@class, "stPlotlyChart")]')
        WebDriverWait(chrome, EXPLICIT_WAIT).until(ec.visibility_of_element_located(plotly_chart_locator))
        assert len(chrome.find_elements(*plotly_chart_locator)) == 1
        assert chrome.find_element(*plotly_chart_locator).is_displayed()
