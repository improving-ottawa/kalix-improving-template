import * as React from 'react';
import Typography from '@mui/material/Typography';
import AddressFormFields, {AddressFormFieldProps} from "./AddressFormFields";

export default function ShippingForm(props: AddressFormFieldProps) {
    return (
        <React.Fragment>
            <Typography variant="h6" gutterBottom>
                Shipping address
            </Typography>
            <AddressFormFields addressWithName={props.addressWithName} setAddressWithName={props.setAddressWithName}
                               hasCountryError={props.hasCountryError}/>
        </React.Fragment>
    );
}