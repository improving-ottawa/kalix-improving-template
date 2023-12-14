import {createAsyncThunk, createSlice, PayloadAction} from "@reduxjs/toolkit";
import {BeginAuthenticationRequest} from "../../../generated/com/example/gateway/api/authentication_service_pb";
import {sendBeginAuthenticationRequest} from "../api/authApi";
import {Product} from "../../screens/productsFlow/ProductsDisplay";
import {AddressWithName, PaymentInfo, PurchasingState} from "./purchasingSlice";

interface AuthState {
    beginAuthStatus: string
}

const initialState: AuthState = {
    beginAuthStatus: ""
}

export const postBeginAuthRequest = createAsyncThunk(
    // TypePrefix must be unique across all slices
    'example/beginAuth',
    async () => {
        // The value we return becomes the `fulfilled` action payload
        return await sendBeginAuthenticationRequest(new BeginAuthenticationRequest());
    }
);

export const purchasingSlice = createSlice({
    name: 'username',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(postBeginAuthRequest.pending, (state) => {
                state.beginAuthStatus = 'loading';
            })
            .addCase(postBeginAuthRequest.fulfilled, (state, response) => {
                state.beginAuthStatus = 'none';
                response.payload
            })
    },
})