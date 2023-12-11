import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import * as React from "react";
import {PostalCode} from "../../../../generated/com/example/common/domain/address_pb";
import {AddressWithName} from "../../../redux/slices/purchasingSlice";
import {PropsWithChildren, useState} from "react";
import {FormControl, FormHelperText, InputLabel, MenuItem, Select} from "@mui/material";
import {Countries, ProvinceStates} from "../../../utils";

export interface AddressFormFieldProps extends PropsWithChildren<any> {
    addressWithName: AddressWithName
    setAddressWithName: React.Dispatch<React.SetStateAction<AddressWithName>>
    hasCountryError: boolean
}


export default function AddressFormFields(props: AddressFormFieldProps) {
    const [stateProvinceError, setStateProvinceError] = useState<boolean>(true)
    const [postalCodeValidationError, setPostalCodeValidationError] = useState<string | undefined>(undefined)

    const country = props.addressWithName.address.getCountry()

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
            <FormControl fullWidth required style={{marginTop: "8px", marginBottom: "4px"}}>
                <InputLabel id="companyStateProvinceField">Country</InputLabel>
                <Select fullWidth
                        required
                        value={props.addressWithName.address.getStateProvince()}
                        labelId="companyStateProvinceField"
                        error={stateProvinceError}
                        onChange={(e) => {
                            if (stateProvinceError) setStateProvinceError(false)
                            props.setAddressWithName({
                                ...props.addressWithName,
                                address: props.addressWithName.address.setStateProvince(e.target.value)
                            })
                        }}
                        id="changeStateProvince" label="State/Province" variant="outlined"
                >
                    {country ? ProvinceStates.get(country)?.map(ps =>
                        <MenuItem divider value={ps}>{ps}</MenuItem>
                    ) : []}
                </Select>
                <FormHelperText>{stateProvinceError ? `${country === "Canada" ? "Province/Territory" : "State"} is required` : ""}</FormHelperText>
            </FormControl>
        </Grid>
        <Grid item xs={12} sm={6}>
            <FormControl fullWidth required style={{marginTop: "8px", marginBottom: "4px"}}
                         error={props.hasCountryError}>
                <InputLabel id="companyCountryField">Country</InputLabel>
                <Select fullWidth
                        required
                        labelId="companyCountryField"
                        value={country}
                        onChange={(e) => {
                            props.setAddressWithName({
                                ...props.addressWithName,
                                address: props.addressWithName.address
                                    .setCountry(e.target.value)
                                    .setPostalCode(new PostalCode())
                                    .setStateProvince("")
                            })
                            setStateProvinceError(true)
                        }}
                        id="changeCountry" label="Country" variant="outlined"
                >
                    {Countries.map(country =>
                        <MenuItem divider value={country}>{country}</MenuItem>
                    )}
                </Select>
                <FormHelperText>{props.hasCountryError ? "Country is required" : ""}</FormHelperText>
            </FormControl>
        </Grid>
        <Grid item xs={12} sm={6}>
            <TextField
                required
                id="zip"
                name="zip"
                label={country === "Canada" ? "Postal Code" : "ZIP Code"}
                fullWidth
                autoComplete="shipping postal-code"
                variant="standard"
                error={postalCodeValidationError !== undefined}
                helperText={postalCodeValidationError ?? ""}
                onChange={(e) => {
                    const input = e.target.value
                    const regexCA = /^[ABCEGHJ-NPRSTVXY]\d[ABCEGHJ-NPRSTV-Z][ -]?\d[ABCEGHJ-NPRSTV-Z]\d$/i;
                    const regexUS = /^\d\d\d\d\d$/i;
                    const forCA = props.addressWithName.address.getCountry() === "Canada"
                    const validation = forCA ?
                        regexCA.exec(input) :
                        regexUS.exec(input)
                    if (validation !== null) {
                        var pc = new PostalCode()
                        pc = forCA ? pc.setCaPostalCodeMessage(input) : pc.setUsPostalCodeMessage(input)
                        props.setAddressWithName({
                            ...props.addressWithName,
                            address: props.addressWithName.address.setPostalCode(pc)
                        })
                        setPostalCodeValidationError(undefined)
                    } else setPostalCodeValidationError(`You must use a correctly formatted ${country === "Canada" ? "Postal Code" : "ZIP Code"} for the region specified`)
                }}
            />
        </Grid>
    </Grid>
}