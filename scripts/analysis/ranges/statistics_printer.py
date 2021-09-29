from collections import defaultdict
import argparse
import pandas as pd

TOTAL = "TOTAL"
OTHER = "other"


def main():
    args = parse_args()
    ranges_df = pd.read_csv(args.input, sep='\t')

    ranges_dict = defaultdict(dict)
    for column_name in ranges_df.columns[1:]:
        range, context = column_name.split(",")
        ranges_dict[range][context] = ranges_df[column_name].sum()
    df_final = pd.DataFrame.from_dict(ranges_dict, orient='index')

    # add total sums
    df_final.loc[TOTAL] = df_final.sum(axis=0)
    df_final.loc[:, TOTAL] = df_final.sum(axis=1)

    # reorder columns
    columns_order_temp = df_final.sort_values(by=TOTAL, ascending=False, axis=1).columns
    columns_order = columns_order_temp.drop([OTHER, TOTAL]).append(pd.Index([OTHER, TOTAL]))
    df_final = df_final[columns_order]

    # reorder rows
    df_final = df_final.sort_values(by=TOTAL, ascending=False)[1:]
    df_final.loc[TOTAL] = df_final.sum(axis=0)

    print(df_final)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Path to results of the ranges analysis plugin")
    return parser.parse_args()


if __name__ == "__main__":
    main()
