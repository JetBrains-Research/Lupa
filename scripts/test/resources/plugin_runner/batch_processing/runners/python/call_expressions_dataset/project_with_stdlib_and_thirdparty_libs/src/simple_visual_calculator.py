from math import cos, sin

import streamlit as st

from utils.option import Option

if __name__ == '__main__':
    st.header('Simple calculator')

    option = Option(st.selectbox('Command:', options=Option.values()))

    number = st.number_input('Number:')

    if option == Option.SIN:
        st.write(f'sin({number}) = {sin(number)}')
    elif option == Option.COS:
        st.write(f'cos({number}) = {cos(number)}')
    elif option == Option.ABS:
        st.write(f'abs({number}) = {abs(number)}')
    else:
        st.error(f'Unknown option: {option}.')
