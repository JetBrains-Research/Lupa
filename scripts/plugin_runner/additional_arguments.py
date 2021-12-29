import argparse

from typing import List, Dict


class AdditionalArguments(argparse.Action):
    def __call__(self, parser, namespace, values, option_string=None):
        setattr(namespace, self.dest, {})

        for value in values:
            key, value = value.split('=')
            getattr(namespace, self.dest)[key] = value

    @staticmethod
    def parse_additional_arguments(additional_arguments: Dict[str, str]) -> List[str]:
        arguments = []
        for key, value in additional_arguments.items():
            arguments.append(f'-P{key}={value}')
        return arguments
