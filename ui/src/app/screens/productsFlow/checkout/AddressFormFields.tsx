import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import * as React from "react";
import {PostalCode} from "../../../../generated/com/example/common/domain/address_pb";
import {AddressWithName} from "../../../redux/slices/purchasingSlice";
import {PropsWithChildren, useEffect, useState} from "react";
import typia, {IValidation} from "typia";
import {FormControl, FormHelperText, InputLabel, MenuItem, Select} from "@mui/material";

export interface AddressFormFieldProps extends PropsWithChildren<any> {
    addressWithName: AddressWithName
    setAddressWithName: React.Dispatch<React.SetStateAction<AddressWithName>>
    hasCountryError: boolean
}

export default function AddressFormFields(props: AddressFormFieldProps) {
    const [postalCodeValidationError, setPostalCodeValidationError] = useState<string | undefined>(undefined)


    return <Grid container spacing={3}>
        <Grid item xs={12} sm={6}>
            <TextField
                required
                id="firstName"
                name="firstName"
                label="First name"
                fullWidth
                autoComplete="given-name"
                variant="standard"
                onChange={(e) => {
                    props.setAddressWithName({...props.addressWithName, firstName: e.target.value})
                }}
            />
        </Grid>
        <Grid item xs={12} sm={6}>
            <TextField
                required
                id="lastName"
                name="lastName"
                label="Last name"
                fullWidth
                autoComplete="family-name"
                variant="standard"
                onChange={(e) => {
                    props.setAddressWithName({...props.addressWithName, lastName: e.target.value})
                }}
            />
        </Grid>
        <Grid item xs={12}>
            <TextField
                required
                id="address1"
                name="address1"
                label="Address line 1"
                fullWidth
                autoComplete="shipping address-line1"
                variant="standard"
                onChange={(e) => {
                    props.setAddressWithName({
                        ...props.addressWithName,
                        address: props.addressWithName.address.setLine1(e.target.value)
                    })
                }}
            />
        </Grid>
        <Grid item xs={12}>
            <TextField
                id="address2"
                name="address2"
                label="Address line 2"
                fullWidth
                autoComplete="shipping address-line2"
                variant="standard"
                onChange={(e) => {
                    props.setAddressWithName({
                        ...props.addressWithName,
                        address: props.addressWithName.address.setLine2(e.target.value)
                    })
                }}
            />
        </Grid>
        <Grid item xs={12} sm={6}>
            <TextField
                required
                id="city"
                name="city"
                label="City"
                fullWidth
                autoComplete="shipping address-level2"
                variant="standard"
                onChange={(e) => {
                    props.setAddressWithName({
                        ...props.addressWithName,
                        address: props.addressWithName.address.setCity(e.target.value)
                    })
                }}
            />
        </Grid>
        <Grid item xs={12} sm={6}>
            <TextField
                id="state"
                name="state"
                label="State/Province/Region"
                fullWidth
                variant="standard"
                onChange={(e) => {
                    props.setAddressWithName({
                        ...props.addressWithName,
                        address: props.addressWithName.address.setStateProvince(e.target.value)
                    })
                }}
            />
        </Grid>
        <Grid item xs={12} sm={6}>
            <FormControl fullWidth required style={{marginTop: "8px", marginBottom: "4px"}}
                         error={props.hasCountryError}>
                <InputLabel id="companyCountryField">Country</InputLabel>
                <Select fullWidth
                        required
                        value={props.addressWithName.address.getCountry()}
                        labelId="companyCountryField"
                        onChange={(e) => {
                            props.setAddressWithName({
                                ...props.addressWithName,
                                address: props.addressWithName.address.setCountry(e.target.value)
                            })
                        }}
                        id="changeCountry" label="Country" variant="outlined"
                >
                    <MenuItem divider value={"Canada"}>Canada</MenuItem>
                    <MenuItem divider value={"US"}>US</MenuItem>
                </Select>
                <FormHelperText>{props.hasCountryError ? "Country is required" : ""}</FormHelperText>
            </FormControl>
        </Grid>
        <Grid item xs={12} sm={6}>
            <TextField
                required
                id="zip"
                name="zip"
                label="Zip / Postal code"
                fullWidth
                autoComplete="shipping postal-code"
                variant="standard"
                error={postalCodeValidationError !== undefined}
                helperText={!postalCodeValidationError ?? postalCodeValidationError}
                onChange={(e) => {
                    const validation = props.addressWithName.address.getCountry() === "Canada" ?
                        typia.protobuf.validateEncode<PostalCode>(e.target.value) :
                        props.addressWithName.address.getCountry() !== "US" ?
                            typia.protobuf.validateEncode<PostalCode>(e.target.value) : typia.protobuf.validateEncode<PostalCode>("")
                    if (validation.success) {
                        const postalCode = new PostalCode()
                        postalCode.setCaPostalCodeMessage(e.target.value)
                        props.setAddressWithName({
                            ...props.addressWithName,
                            address: props.addressWithName.address.setPostalCode(postalCode)
                        })
                    } else setPostalCodeValidationError(validation.errors.join(", "))
                }}
            />
        </Grid>
    </Grid>
}