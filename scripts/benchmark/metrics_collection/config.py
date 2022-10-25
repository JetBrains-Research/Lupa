from benchmark.metrics_collection.metrics import MetricArgument, MetricName

SCHEMA = {
    'metrics': {
        'type': 'dict',
        'schema': {
            MetricName.NUMBER_OF_FILES.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {},
            },
            MetricName.NUMBER_OF_DEPENDENCIES.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {},
            },
            MetricName.FILE_SIZE.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {},
            },
            MetricName.NUMBER_OF_LINES.value: {
                'type': 'dict',
                'empty': True,
                'nullable': True,
                'schema': {
                    MetricArgument.IGNORE_COMMENTS.value: {'type': 'boolean'},
                    MetricArgument.IGNORE_EMPTY_LINES.value: {'type': 'boolean'},
                },
            },
        },
    },
}
