import * as React from 'react';
import Typography from '@mui/material/Typography';
import AddressFormFields, {AddressFormFieldProps} from "./AddressFormFields";
import {useState} from "react";
import {AddressWithName} from "../../../redux/slices/purchasingSlice";
import {Address} from "../../../../generated/com/example/common/domain/address_pb";

export default function ShippingForm(props: AddressFormFieldProps) {
    const [addressWithName, setAddressWithName] = useState<AddressWithName>({address: new Address()})

    return (
        <React.Fragment>
            <Typography variant="h6" gutterBottom>
                Shipping address
            </Typography>
            <AddressFormFields addressWithName={props.addressWithName} setAddressWithName={props.setAddressWithName}/>
        </React.Fragment>
    );
}