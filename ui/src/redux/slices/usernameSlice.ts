import {createAsyncThunk, createSlice, PayloadAction} from '@reduxjs/toolkit'
import type {AppThunk, RootState} from '../store'
import {fetchUsername} from "../api";

// Define a type for the slice state
export interface UsernameState {
    name: string
}

// Define the initial state using that type
const initialState: UsernameState = {
    name: "[no name]"
}

// A template for an async action to an external API
export const changeNameAsync = createAsyncThunk(
    'counter/fetchCount',
    async (name: string) => {
        const response = await fetchUsername(name);
        // The value we return becomes the `fulfilled` action payload
        return response.data;
    }
);

export const usernameSlice = createSlice({
    name: 'username',
    // `createSlice` will infer the state type from the `initialState` argument
    initialState,
    reducers: {
        changeName: (state, action: PayloadAction<string>) => {
            state.name = action.payload
        },
    },
    // template for external API call reducer
    //extraReducers: (builder) => {
    //    builder
    //        .addCase(changeNameAsync.pending, (state) => {
    //            state.status = 'loading';
    //        })
    //},
})

export const { changeName } = usernameSlice.actions

// Other code such as selectors can use the imported `RootState` type
export const selectUsername = (state: RootState) => state.username.name

export const setUsername = (name: string): AppThunk =>
    (dispatch, getState) => {
        console.log("changing name from " + getState().username.name + " to " + name)
        dispatch(changeName(name))
    }
export default usernameSlice.reducer