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
import {FormControl, FormHelperText, IconButton, InputAdornment, InputLabel, OutlinedInput} from "@mui/material";
import dayjs from "dayjs";
import {useState} from "react";
import {Visibility, VisibilityOff} from "@mui/icons-material";
import ShippingBillingFooter from "./ShippingBillingFooter";

interface BillingFormProps {
    showAddress: boolean
    setShowAddress: React.Dispatch<React.SetStateAction<boolean>>
}

export default function BillingForm(props: AddressFormFieldProps & BillingFormProps) {
    const paymentInfo = useAppSelector(selectPurchasingState).paymentInfo
    const dispatch = useAppDispatch()

    const [showCardNumber, setShowCardNumber] = useState<boolean>(false)
    const [showCVV, setShowCVV] = useState<boolean>(false)

    const cardNumberRegex = /^\d\d\d\d\d\d\d\d\d\d\d\d\d\d\d\d$/i
    const cvvRegex = /^\d\d\d$/i

    return (
        <ShippingBillingFooter handleBack={props.handleBack} handleNext={props.handleNext}
                               showBack={props.showBack}>
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
                        variant="outlined"
                        value={paymentInfo?.cardHolder}
                        onChange={(e) => {
                            dispatch(changePaymentInfo({...paymentInfo, cardHolder: e.target.value}))
                        }}
                    />
                </Grid>
                <Grid item xs={12} md={6}>
                    <FormControl variant="outlined">
                        <InputLabel htmlFor="adornment-card-number">Card Number</InputLabel>
                        <OutlinedInput
                            id="adornment-card-number"
                            type={showCardNumber ? 'text' : 'password'}
                            value={paymentInfo?.cardNumber}
                            label="Card Number"
                            endAdornment={
                                <InputAdornment position="end">
                                    <IconButton
                                        aria-label="toggle password visibility"
                                        onClick={() => {
                                            setShowCardNumber(!showCardNumber)
                                        }}
                                        onMouseDown={(e) => e.preventDefault()}
                                        edge="end"
                                    >
                                        {showCardNumber ? <VisibilityOff/> : <Visibility/>}
                                    </IconButton>
                                </InputAdornment>
                            }
                            onChange={(e) => {
                                dispatch(changePaymentInfo({...paymentInfo, cardNumber: e.target.value}))
                            }}
                            error={
                                paymentInfo?.cardNumber ?
                                    (paymentInfo.cardNumber.length < 16 || cardNumberRegex.exec(paymentInfo.cardNumber) === null) :
                                    false
                            }
                            inputProps={{maxLength: 16}}
                        />
                    </FormControl>
                    <FormHelperText id="card-number-helper-text">Must be a 16 digit credit card number</FormHelperText>
                </Grid>
                <Grid item xs={12} md={6}>
                    <DatePicker sx={{backgroundColor: "white"}}
                                label="Expiry date"
                                disablePast
                                views={['year', 'month']}
                                value={paymentInfo?.expiryDate ? dayjs(paymentInfo?.expiryDate) : dayjs().add(6, 'months')}
                                onChange={(e) => {
                                    dispatch(changePaymentInfo({
                                        ...paymentInfo,
                                        expiryDate: e?.toDate()
                                    }))

                                }}
                    />
                    <FormHelperText id="biweekly-helper-text" style={{
                        paddingLeft: "10px",
                        color: "red"
                    }}>{!paymentInfo?.expiryDate && "Expiry Date is required"}</FormHelperText>
                </Grid>
                <Grid item xs={12} md={6}>
                    <FormControl variant="outlined">
                        <InputLabel htmlFor="adornment-cvv">CVV</InputLabel>
                        <OutlinedInput
                            id="adornment-cvv"
                            type={showCVV ? 'text' : 'password'}
                            value={paymentInfo?.cvv}
                            label="CVV"
                            endAdornment={
                                <InputAdornment position="end">
                                    <IconButton
                                        aria-label="toggle password visibility"
                                        onClick={() => {
                                            setShowCVV(!showCVV)
                                        }}
                                        onMouseDown={(e) => e.preventDefault()}
                                    >
                                        {showCVV ? <VisibilityOff/> : <Visibility/>}
                                    </IconButton>
                                </InputAdornment>
                            }
                            onChange={(e) => {
                                dispatch(changePaymentInfo({...paymentInfo, cvv: e.target.value}))
                            }}
                            error={
                                paymentInfo?.cvv ?
                                    (paymentInfo.cvv.length < 3 || cvvRegex.exec(paymentInfo.cvv) === null) :
                                    false
                            }
                            inputProps={{maxLength: 3}}
                        />
                    </FormControl>
                    <FormHelperText id="cvv-helper-text">Last three digits on signature strip</FormHelperText>
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
        </ShippingBillingFooter>
    );
}