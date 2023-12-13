// See https://redux.js.org/tutorials/typescript-quick-start

import {Action, configureStore, ThunkAction} from '@reduxjs/toolkit'
import purchasingReducer from './slices/purchasingSlice'

export const store = configureStore({
    reducer: {
        purchasing: purchasingReducer
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

