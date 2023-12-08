import {createAsyncThunk, createSlice, PayloadAction} from '@reduxjs/toolkit'
import {sendDoNothingTwiceCommand} from "../api/exampleApi";
import {DoNothingTwiceCommand} from "../../../generated/com/example/gateway/domain/gateway_commands_pb";
import {Address} from "../../../generated/com/example/common/domain/address_pb";
import {Product} from "../../screens/productsFlow/ProductsDisplay";
import {RootState} from "../store";

export interface PaymentInfo {
    cardType: string
    cardHolder: string
    cardNumber: string
    expiryDate: Date
}

export interface PurchasingState {
    products: Product[]

    shippingAddress?: Address
    billingAddress?: Address
    paymentInfo?: PaymentInfo
}

// Define the initial state using that type
const initialState: PurchasingState = {
    products: new Array<Product>(),
    shippingAddress: undefined,
    billingAddress: undefined,
    paymentInfo: undefined
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

export const purchasingSlice = createSlice({
    name: 'username',
    initialState,
    reducers: {
        addProduct: (state, action: PayloadAction<Product>) => {
            state.products.push(action.payload)
        },
        changeShippingAddress: (state, action: PayloadAction<Address>) => {
            state.shippingAddress = action.payload
        },
        changeBillingAddress: (state, action: PayloadAction<Address>) => {
            state.billingAddress = action.payload
        },
        changePaymentInfo: (state, action: PayloadAction<PaymentInfo>) => {
            state.paymentInfo = action.payload
        },
    },
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

//export reducer actions here
export const {
    addProduct,
    changeShippingAddress,
    changeBillingAddress,
    changePaymentInfo
} = purchasingSlice.actions

export const selectProducts = (state: RootState) => state.purchasing.products

export default purchasingSlice.reducer