// See https://redux.js.org/tutorials/typescript-quick-start

import {Action, configureStore, ThunkAction} from '@reduxjs/toolkit'
import exampleReducer from './slices/exampleSlice'
export const store = configureStore({
    reducer: {
        example: exampleReducer
    }
})

// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch
export type AppThunk<ReturnType = void> = ThunkAction<
    ReturnType,
    RootState,
    unknown,
    Action<string>
>;

