import pytest
from selenium.webdriver import Chrome
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager, ChromeType


@pytest.fixture
def chrome() -> Chrome:
    chrome_service = Service(ChromeDriverManager(chrome_type=ChromeType.CHROMIUM).install())

    chrome_options = Options()
    options = [
        "--headless=new",
        "--disable-gpu",
        "--window-size=1920,1080",
        "--ignore-certificate-errors",
        "--disable-extensions",
        "--no-sandbox",
        "--disable-dev-shm-usage",
    ]
    for option in options:
        chrome_options.add_argument(option)

    driver = Chrome(service=chrome_service, options=chrome_options)
    yield driver
    driver.quit()
