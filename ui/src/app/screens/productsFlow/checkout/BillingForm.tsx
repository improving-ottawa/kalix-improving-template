import * as React from 'react';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import AddressFormFields, {AddressFormFieldProps} from "./AddressFormFields";
import {changePaymentInfo, selectPurchasingState} from "../../../redux/slices/purchasingSlice";
import {useAppDispatch, useAppSelector} from "../../../redux/hooks";
import {DatePicker} from "@mui/x-date-pickers";
import {FormHelperText} from "@mui/material";

interface BillingFormProps {
    showAddress: boolean
    setShowAddress: React.Dispatch<React.SetStateAction<boolean>>
}

export default function BillingForm(props: AddressFormFieldProps & BillingFormProps) {
    const paymentInfo = useAppSelector(selectPurchasingState).paymentInfo
    const dispatch = useAppDispatch()

    return (
        <React.Fragment>
            <Typography variant="h6" gutterBottom>
                Payment method
            </Typography>
            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="cardName"
                        label="Name on card"
                        fullWidth
                        autoComplete="cc-name"
                        variant="standard"
                        value={paymentInfo?.cardHolder}
                        onChange={(e) => {
                            dispatch(changePaymentInfo({...paymentInfo, cardHolder: e.target.value}))
                        }}
                    />
                </Grid>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="cardNumber"
                        label="Card number"
                        fullWidth
                        autoComplete="cc-number"
                        variant="standard"
                        value={paymentInfo?.cardNumber}
                        error={paymentInfo?.cardNumber ? paymentInfo?.cardNumber.length < 12 : false}
                        onChange={(e) => {
                            dispatch(changePaymentInfo({...paymentInfo, cardNumber: e.target.value}))
                        }}
                        inputProps={{maxLength: 12}}
                        helperText="Must be a 12 digit credit card number"
                    />
                </Grid>
                <Grid item xs={12} md={6}>
                    <DatePicker sx={{backgroundColor: "white"}}
                                label="Expiry date"
                                value={paymentInfo?.expiryDate}
                                onChange={(e) => {
                                    dispatch(changePaymentInfo({
                                        ...paymentInfo,
                                        expiryDate: e ?? undefined
                                    }))

                                }}
                    />
                    <FormHelperText id="biweekly-helper-text" style={{
                        paddingLeft: "10px",
                        color: "red"
                    }}>{!paymentInfo?.expiryDate && "Expiry Date is required"}</FormHelperText>
                </Grid>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="cvv"
                        label="CVV"
                        helperText="Last three digits on signature strip"
                        fullWidth
                        autoComplete="cc-csc"
                        variant="standard"
                        error={paymentInfo?.cvv ? paymentInfo.cvv.length < 3 : false}
                        inputProps={{maxLength: 3}}
                        value={paymentInfo?.cvv}
                        onChange={(e) => {
                            dispatch(changePaymentInfo({...paymentInfo, cvv: e.target.value}))
                        }}
                    />
                </Grid>
                <Grid item xs={12}>
                    <FormControlLabel
                        onChange={() => props.setShowAddress(!props.showAddress)}
                        control={<Checkbox name="saveAddress"
                                           checked={!props.showAddress}/>}
                        label="Use shipping address for payment details"
                    />
                    {props.showAddress &&
                        <AddressFormFields addressWithName={props.addressWithName}
                                           setAddressWithName={props.setAddressWithName}
                                           hasCountryError={props.hasCountryError}/>}
                </Grid>
            </Grid>
        </React.Fragment>
    );
}