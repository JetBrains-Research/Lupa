from dataclasses import dataclass
from typing import Optional, List


@dataclass
class SourceCodeManagement:
    url: Optional[str]


@dataclass
class Package:
    group_id: str
    artifact_id: str
    name: Optional[str]
    url: Optional[str]
    scm: Optional[SourceCodeManagement]


@dataclass
class PackageSearchResponse:
    packages: List[Package]
