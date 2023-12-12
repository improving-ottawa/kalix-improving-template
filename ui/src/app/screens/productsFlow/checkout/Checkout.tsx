import * as React from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import AppBar from '@mui/material/AppBar';
import Container from '@mui/material/Container';
import Toolbar from '@mui/material/Toolbar';
import Paper from '@mui/material/Paper';
import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Typography from '@mui/material/Typography';
import ShippingForm from './ShippingForm';
import BillingForm from './BillingForm';
import Review from './Review';
import {Copyright} from "../../styledComponents/copyright";
import {useState} from "react";
import {
    AddressWithName,
    changeBillingAddress,
    changeShippingAddress,
} from "../../../redux/slices/purchasingSlice";
import {Address} from "../../../../generated/com/example/common/domain/address_pb";
import {useAppDispatch} from "../../../redux/hooks";

const steps = ['Shipping address', 'Payment details', 'Review your order'];

export default function Checkout() {
    const dispatch = useAppDispatch()
    const [activeStep, setActiveStep] = useState<number>(0);
    const [showAddress, setShowAddress] = useState<boolean>(true)

    const [billingAddressWithName, setBillingAddressWithName] = useState<AddressWithName>({address: new Address()})
    const [shippingAddressWithName, setShippingAddressWithName] = useState<AddressWithName>({address: new Address()})
    const hasBillingCountryError = billingAddressWithName.address.getCountry().length === 0
    const hasShippingCountryError = shippingAddressWithName.address.getCountry().length === 0

    function getStepContent(step: number) {
        switch (step) {
            case 0:
                return <ShippingForm addressWithName={shippingAddressWithName}
                                     setAddressWithName={setShippingAddressWithName}
                                     hasCountryError={hasShippingCountryError}
                                     handleNext={() => {
                                         dispatch(changeShippingAddress(shippingAddressWithName))
                                         setActiveStep(activeStep + 1)
                                     }}
                                     handleBack={() => {
                                         setActiveStep(activeStep - 1)
                                     }}
                                     showBack={false}
                />;
            case 1:
                return <BillingForm addressWithName={billingAddressWithName}
                                    setAddressWithName={setBillingAddressWithName}
                                    hasCountryError={hasBillingCountryError}
                                    showAddress={showAddress} setShowAddress={setShowAddress}
                                    handleNext={() => {
                                        dispatch(changeBillingAddress(billingAddressWithName))
                                        setActiveStep(activeStep + 1)
                                    }}
                                    handleBack={() => {
                                        setActiveStep(activeStep - 1)
                                    }}/>;
            case 2:
                return <Review/>;
            default:
                throw new Error('Unknown step');
        }
    }

    return (
        <React.Fragment>
            <CssBaseline/>
            <AppBar
                position="absolute"
                color="default"
                elevation={0}
                sx={{
                    position: 'relative',
                    borderBottom: (t) => `1px solid ${t.palette.divider}`,
                }}
            >
                <Toolbar>
                    <Typography variant="h6" color="inherit" noWrap>
                        Company name
                    </Typography>
                </Toolbar>
            </AppBar>
            <Container component="main" maxWidth="sm" sx={{mb: 4}}>
                <Paper variant="outlined" sx={{my: {xs: 3, md: 6}, p: {xs: 2, md: 3}}}>
                    <Typography component="h1" variant="h4" align="center">
                        Checkout
                    </Typography>
                    <Stepper activeStep={activeStep} sx={{pt: 3, pb: 5}}>
                        {steps.map((label, index) => (
                            <Step key={label} onClick={() => {
                                setActiveStep(index)
                            }}>
                                <StepLabel>{label}</StepLabel>
                            </Step>
                        ))}
                    </Stepper>
                    {activeStep === steps.length ? (
                        <React.Fragment>
                            <Typography variant="h5" gutterBottom>
                                Thank you for your order.
                            </Typography>
                            <Typography variant="subtitle1">
                                Your order number is #2001539. We have emailed your order
                                confirmation, and will send you an update when your order has
                                shipped.
                            </Typography>
                        </React.Fragment>
                    ) : (
                        <React.Fragment>
                            {getStepContent(activeStep)}
                        </React.Fragment>
                    )}
                </Paper>
                <Copyright/>
            </Container>
        </React.Fragment>
    );
}