import * as React from 'react';
import Typography from '@mui/material/Typography';
import AddressFormFields, {AddressFormFieldProps} from "./AddressFormFields";
import ShippingBillingFooter, {ShippingBillingFooterProps} from "./ShippingBillingFooter";

export default function ShippingForm(props: AddressFormFieldProps & ShippingBillingFooterProps) {

    return (
        <ShippingBillingFooter handleBack={props.handleBack} handleNext={props.handleNext}
                               showBack={props.showBack}>
            <Typography variant="h6" gutterBottom>
                Shipping address
            </Typography>
            <AddressFormFields addressWithName={props.addressWithName} setAddressWithName={props.setAddressWithName}
                               hasCountryError={props.hasCountryError}/>
        </ShippingBillingFooter>
    );
}