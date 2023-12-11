import * as React from 'react';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import AddressFormFields, {AddressFormFieldProps} from "./AddressFormFields";

interface BillingFormProps {
    showAddress: boolean
    setShowAddress: React.Dispatch<React.SetStateAction<boolean>>
}

export default function BillingForm(props: AddressFormFieldProps & BillingFormProps) {

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
                    />
                </Grid>
                <Grid item xs={12} md={6}>
                    <TextField
                        required
                        id="expDate"
                        label="Expiry date"
                        fullWidth
                        autoComplete="cc-exp"
                        variant="standard"
                    />
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