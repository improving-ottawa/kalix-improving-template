import * as React from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Toolbar from '@mui/material/Toolbar';
import Paper from '@mui/material/Paper';
import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import ShippingForm from './ShippingForm';
import BillingForm from './BillingForm';
import Review from './Review';
import {Copyright} from "../../styledComponents/copyright";
import {Link} from "@mui/material";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import {
    AddressWithName,
    changeBillingAddress,
    changeShippingAddress,
    selectPurchasingState
} from "../../../redux/slices/purchasingSlice";
import {Address} from "../../../../generated/com/example/common/domain/address_pb";
import {useAppDispatch, useAppSelector} from "../../../redux/hooks";

const steps = ['Shipping address', 'Payment details', 'Review your order'];

export default function Checkout() {
    const navigate = useNavigate()
    const dispatch = useAppDispatch()
    const [activeStep, setActiveStep] = React.useState(0);
    const [showAddress, setShowAddress] = useState(true)

    const state = useAppSelector(selectPurchasingState)

    const [addressWithName, setAddressWithName] = useState<AddressWithName>({address: new Address()})
    const hasCountryError = addressWithName.address.getCountry().length === 0

    useEffect(() => {
        if (state.shippingAddress) {
        }
    }, [state.shippingAddress?.address, state.billingAddress?.address])

    function getStepContent(step: number) {
        switch (step) {
            case 0:
                return <ShippingForm addressWithName={addressWithName} setAddressWithName={setAddressWithName}
                                     hasCountryError={hasCountryError}/>;
            case 1:
                return <BillingForm addressWithName={addressWithName} setAddressWithName={setAddressWithName}
                                    hasCountryError={hasCountryError}
                                    showAddress={showAddress} setShowAddress={setShowAddress}/>;
            case 2:
                return <Review/>;
            default:
                throw new Error('Unknown step');
        }
    }

    const handleNext = () => {
        if (activeStep === 0) dispatch(changeShippingAddress(addressWithName))
        else if (activeStep === 1) dispatch(changeBillingAddress(addressWithName))
        setAddressWithName({address: new Address()})
        setActiveStep(activeStep + 1);
    };

    const handleBack = () => {
        setActiveStep(activeStep - 1);
        if (activeStep === 0 && state.shippingAddress)
            setAddressWithName(state.shippingAddress)
        else if (activeStep === 1 && state.billingAddress) setAddressWithName(state.billingAddress)
        else setAddressWithName({address: new Address()})
    };

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
                        {steps.map((label) => (
                            <Step key={label}>
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
                            <Box sx={{display: 'flex'}}>
                                <Box sx={{display: 'flex', justifyContent: 'flex-start'}}>
                                    <Link underline="none" onClick={() => navigate("/pricing")}
                                          sx={{mt: 3, ml: 1}}>
                                        {"Continue shopping"}
                                    </Link>
                                </Box>
                                <Box sx={{display: 'flex', marginLeft: 'auto'}}>
                                    {activeStep !== 0 && (
                                        <Button onClick={handleBack} sx={{mt: 3, ml: 1}}>
                                            Back
                                        </Button>
                                    )}
                                    <Button
                                        variant="contained"
                                        onClick={handleNext}
                                        sx={{mt: 3, ml: 1}}
                                    >
                                        {activeStep === steps.length - 1 ? 'Place order' : 'Next'}
                                    </Button>
                                </Box>
                            </Box>
                        </React.Fragment>
                    )}
                </Paper>
                <Copyright/>
            </Container>
        </React.Fragment>
    );
}