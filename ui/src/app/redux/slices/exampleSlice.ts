import {createAsyncThunk, createSlice} from '@reduxjs/toolkit'
import type {AppThunk, RootState} from '../store'
import {sendDoNothingTwiceCommand} from "../api/exampleApi";
import {DoNothingTwiceCommand} from "../../../generated/com/example/gateway/domain/gatewayCommands_pb";

// Define a type for the slice state
export interface ExampleState {
    //your state fields here
}

// Define the initial state using that type
const initialState: ExampleState = {
    //initialization of state fields here
}

// A template for an async action to an external API
export const postDoNothingTwice = createAsyncThunk(
    // TypePrefix must be unique across all slices
    'example/doNothingTwice',
    async () => {
        // The value we return becomes the `fulfilled` action payload
        return await sendDoNothingTwiceCommand(new DoNothingTwiceCommand());
    }
);

export const exampleSlice = createSlice({
    name: 'username',
    // `createSlice` will infer the state type from the `initialState` argument
    initialState,
    reducers: {
        // Use for changing a field based on UX component actions - use Dispatch function to send
        //changeField: (state, action: PayloadAction<string>) => {
        //
        //},
    },
    // template for external API call reducer
    //extraReducers: (builder) => {
    //    builder
    //        .addCase(postDoNothingTwice.pending, (state) => {
    //            state.status = 'loading';
    //        })
    //        .addCase(postDoNothingTwice.pending, (state, response) => {
    //            state.fulfilled = 'none';
    //            state.field = response.payload?.field
    //        })
    //},
})

export const {  } = exampleSlice.actions

// Other code such as selectors can use the imported `RootState` type
// TODO: use a select to get state in a component
//export const selectField = (state: RootState) => state.field.name


export default exampleSlice.reducer