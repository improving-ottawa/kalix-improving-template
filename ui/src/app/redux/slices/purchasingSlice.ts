import {createSlice, PayloadAction} from '@reduxjs/toolkit'
import {Address} from "../../../generated/com/example/common/domain/address_pb";
import {Product} from "../../screens/productsFlow/ProductsDisplay";
import {RootState} from "../store";

export interface AddressWithName {
    address: Address,
    firstName?: string,
    lastName?: string
}

export interface PaymentInfo {
    cvv?: string
    cardHolder?: string
    cardNumber?: string
    expiryDate?: Date
}

export interface PurchasingState {
    products: Product[]

    shippingAddress?: AddressWithName
    billingAddress?: AddressWithName
    paymentInfo?: PaymentInfo
}

// Define the initial state using that type
const initialState: PurchasingState = {
    products: new Array<Product>(),
    shippingAddress: undefined,
    billingAddress: undefined,
    paymentInfo: undefined
}

export const purchasingSlice = createSlice({
    name: 'username',
    initialState,
    reducers: {
        addProduct: (state, action: PayloadAction<Product>) => {
            state.products.push(action.payload)
        },
        changeShippingAddress: (state, action: PayloadAction<AddressWithName>) => {
            state.shippingAddress = action.payload
        },
        changeBillingAddress: (state, action: PayloadAction<AddressWithName>) => {
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

export const selectPurchasingState = (state: RootState) => state.purchasing

export default purchasingSlice.reducer