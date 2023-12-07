import * as React from 'react';
import Typography from '@mui/material/Typography';
import AddressFormFields from "./AddressFormFields";

export default function ShippingForm() {
    return (
        <React.Fragment>
            <Typography variant="h6" gutterBottom>
                Shipping address
            </Typography>
            <AddressFormFields/>
        </React.Fragment>
    );
}