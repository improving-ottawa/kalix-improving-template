import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import {BeginAuthenticationRequest} from "../../../generated/com/example/gateway/api/authentication_service_pb";
import {sendBeginAuthenticationRequest} from "../api/authApi";
import {RootState} from "../store";

interface AuthState {
    beginAuthStatus: string
    authHeaders: Uint8Array | string
}

const initialState: AuthState = {
    beginAuthStatus: "",
    authHeaders: ""
}

export const getBeginAuthResponse = createAsyncThunk(
    // TypePrefix must be unique across all slices
    'example/beginAuth',
    async () => {
        let req = new BeginAuthenticationRequest()
        req = req.setProviderId("local_keycloak")
        req = req.setRedirectUri("http://localhost:9000/pricing")
        // The value we return becomes the `fulfilled` action payload
        return await sendBeginAuthenticationRequest(req);
    }
);

export const authSlice = createSlice({
    name: 'login',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getBeginAuthResponse.pending, (state) => {
                state.beginAuthStatus = 'loading';
            })
            .addCase(getBeginAuthResponse.fulfilled, (state, response) => {
                state.beginAuthStatus = 'none';
                state.authHeaders = response.payload.getData()
                console.log(response.payload.getExtensionsList)
            })
    },
})


//export reducer actions here
export const {} = authSlice.actions

export const selectAuthState = (state: RootState) => state.auth

export default authSlice.reducer