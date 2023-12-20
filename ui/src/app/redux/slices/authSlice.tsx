import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import {sendBeginAuthenticationRequest, sendGetUserRequest} from "../api/authApi";
import {RootState} from "../store";
import {GetUserRequest} from "../../../generated/com/example/gateway/domain/user_domain_pb";
import {decodedJwtToken} from "../api/clients";

interface AuthState {
    beginAuthStatus: string
    getUserStatus: string
    authHeaders: Uint8Array | string
}

const initialState: AuthState = {
    getUserStatus: "none",
    beginAuthStatus: "none",
    authHeaders: ""
}

export const beginAuth = createAsyncThunk(
    // TypePrefix must be unique across all slices
    'example/beginAuth',
    async () => {
        return sendBeginAuthenticationRequest();
    }
);

export const getUser = createAsyncThunk(
    // TypePrefix must be unique across all slices
    'example/getUser',
    async () => {
        const jwt = decodedJwtToken()
        let req = new GetUserRequest()
        req = jwt && jwt.sub ? req.setUserId(jwt.sub) : req
        return sendGetUserRequest(req);
    }
);

export const authSlice = createSlice({
    name: 'login',
    initialState,
    reducers: {},
    extraReducers: (builder) =>
        builder
            .addCase(beginAuth.pending, (state) => {
                state.beginAuthStatus = 'loading';
            })
            .addCase(beginAuth.fulfilled, (state, response) => {
                state.beginAuthStatus = 'none'

                console.log("Success logging in")
            }).addCase(getUser.pending, (state) => {
            state.getUserStatus = 'loading';
        })
            .addCase(getUser.fulfilled, (state, response) => {
                state.getUserStatus = 'none'

                console.log("Success logging in")

                console.log(response.payload.getUserResponse.getUserInfo())
            })
})


//export reducer actions here
export const {} = authSlice.actions

export const selectAuthState = (state: RootState) => state.auth

export default authSlice.reducer