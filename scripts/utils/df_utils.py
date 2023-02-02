from typing import List, Optional

import pandas as pd


def assert_df_equals(
    actual: Optional[pd.DataFrame],
    expected: Optional[pd.DataFrame],
    sort_by_columns: Optional[List[str]] = None,
) -> None:
    if actual is None:
        # assert_frame_equal(None, None) will raise an error, but None equals None
        assert expected is None
    else:
        if sort_by_columns is not None:
            actual = actual.sort_values(by=sort_by_columns).reset_index(drop=True)
            expected = expected.sort_values(by=sort_by_columns).reset_index(drop=True)

        actual = actual.reindex(sorted(actual.columns), axis=1)
        expected = expected.reindex(sorted(expected.columns), axis=1)

        pd.testing.assert_frame_equal(actual, expected)
