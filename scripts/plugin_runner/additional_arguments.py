import argparse
from typing import Dict, List, Optional


class AdditionalArguments(argparse.Action):
    def __call__(self, parser, namespace, values, option_string=None):
        setattr(namespace, self.dest, {})

        for value in values:
            key, value = value.split('=')
            getattr(namespace, self.dest)[key] = value

    @staticmethod
    def parse_additional_arguments(additional_arguments: Optional[Dict[str, str]]) -> List[str]:
        arguments = []
        if additional_arguments is None:
            return arguments
        for key, value in additional_arguments.items():
            arguments.append(f'-P{key}={value}')
        return arguments
