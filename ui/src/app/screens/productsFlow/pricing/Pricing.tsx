import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import CssBaseline from '@mui/material/CssBaseline';
import Grid from '@mui/material/Grid';
import StarIcon from '@mui/icons-material/StarBorder';
import Typography from '@mui/material/Typography';
import Link from '@mui/material/Link';
import GlobalStyles from '@mui/material/GlobalStyles';
import Container from '@mui/material/Container';
import {AppBar} from "@mui/material";
import {TopNav} from "../../styledComponents/navBars";
import {Products, productsDisplay} from "../ProductsDisplay";
import {NavigateFunction, useNavigate} from "react-router-dom";
import {useDispatch} from "react-redux";
import {addProduct, PurchasingState} from "../../../redux/slices/purchasingSlice";
import {Copyright} from "../../styledComponents/copyright";
import Cookies from "cookies-ts";
import {Dispatch, useEffect} from "react";
import {AuthState, getUser} from "../../../redux/slices/authSlice";
import {AppDispatch} from "../../../redux/store";
import {retrieveIdentity} from "../../../identity";
import {AnyAction, ThunkDispatch} from "@reduxjs/toolkit";

const footers = [
    {
        title: 'Legal',
        description: ['Privacy policy', 'Terms of use'],
    },
];

const initializeStorageForAuth = (
    dispatch: ThunkDispatch<{ purchasing: PurchasingState, auth: AuthState }, undefined, AnyAction> & Dispatch<AnyAction>,
    navigate: NavigateFunction) => {
    const cookies = new Cookies()

    const maybeExistingToken = sessionStorage.getItem('csrfToken')
    const csrfTokenCookie = cookies.get('csrfToken')

    let validCsrfToken = false

    if (maybeExistingToken && maybeExistingToken.length !== 0) {
        console.log("Using existing CSRF token in sessionStorage.")
        validCsrfToken = true
    } else if (csrfTokenCookie) {
        console.log("Storing (and deleting) CSRF token...")
        sessionStorage.setItem('csrfToken', csrfTokenCookie)
        cookies.remove('csrfToken')
        validCsrfToken = true
    }

    const identity = retrieveIdentity()
    const epochSeconds = (new Date()).getUTCSeconds()

    // Check that: we have a CSRF token, we have an identity, and the identity is not expired...
    // Note: the `identity.exp`(iration) is in Unix epoch _seconds_ (not milliseconds!)
    if (validCsrfToken && identity && identity.exp > epochSeconds) {
        dispatch(getUser())
    } else {
        if (validCsrfToken && identity) {
            const expiredDate = new Date(identity.exp)
            console.log(`Expired Identity: ${identity} (expired at: ${expiredDate}).`)
        } else if (validCsrfToken) {
            console.log(`Invalid or missing Identity.`)
        } else {
            console.log(`Missing CSRF token.`)
        }

        // Send the user to the login page
        navigate("/")
    }
}

export default function Pricing() {
    const navigate = useNavigate()
    const dispatch = useDispatch<AppDispatch>()

    useEffect(() => initializeStorageForAuth(dispatch, navigate), [])

    return (
        <Box>
            <GlobalStyles styles={{ul: {margin: 0, padding: 0, listStyle: 'none'}}}/>
            <CssBaseline/>
            <AppBar
                position="static"
                color="default"
                elevation={0}
                sx={{borderBottom: (theme) => `1px solid ${theme.palette.divider}`}}
            >
                <TopNav/>
            </AppBar>
            <Container disableGutters maxWidth="sm" component="main" sx={{pt: 8, pb: 6}}>
                <Typography
                    component="h1"
                    variant="h2"
                    align="center"
                    color="text.primary"
                    gutterBottom
                >
                    Products
                </Typography>
                <Typography variant="h5" align="center" color="text.secondary" component="p">
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce venenatis,
                    lacus suscipit malesuada maximus, nisl tellus posuere neque.
                </Typography>
            </Container>
            <Container maxWidth="md" component="main">
                <Grid container spacing={5} alignItems="flex-end">
                    {productsDisplay.map((product) => (
                        <Grid
                            item
                            key={product.title}
                            xs={12}
                            sm={product.title === 'Lorem ipsum C' ? 12 : 6}
                            md={4}
                        >
                            <Card>
                                <CardHeader
                                    title={product.title}
                                    subheader={product.subheader}
                                    titleTypographyProps={{align: 'center'}}
                                    action={product.title === 'Lorem ipsum A' ? <StarIcon/> : null}
                                    subheaderTypographyProps={{
                                        align: 'center',
                                    }}
                                    sx={{
                                        backgroundColor: (theme) =>
                                            theme.palette.mode === 'light'
                                                ? theme.palette.grey[200]
                                                : theme.palette.grey[700],
                                    }}
                                />
                                <CardContent>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'center',
                                            alignItems: 'baseline',
                                            mb: 2,
                                        }}
                                    >
                                        <Typography component="h2" variant="h3" color="text.primary">
                                            ${product.price}
                                        </Typography>
                                        <Typography variant="h6" color="text.secondary">
                                            /mo
                                        </Typography>
                                    </Box>
                                    <ul>
                                        {product.description.map((line) => (
                                            <Typography
                                                component="li"
                                                variant="subtitle1"
                                                align="center"
                                                key={line}
                                            >
                                                {line}
                                            </Typography>
                                        ))}
                                    </ul>
                                </CardContent>
                                <CardActions>
                                    <Button
                                        onClick={() => {
                                            const productToAdd = Products.find(_ => _.name === product.title)
                                            if (productToAdd) dispatch(addProduct(productToAdd))
                                            navigate("/checkout")
                                        }}
                                        fullWidth
                                        variant={product.buttonVariant as 'outlined' | 'contained'}
                                    >
                                        {product.buttonText}
                                    </Button>
                                </CardActions>
                            </Card>
                        </Grid>
                    ))}
                </Grid>
            </Container>
            <Container
                maxWidth="md"
                component="footer"
                sx={{
                    borderTop: (theme) => `1px solid ${theme.palette.divider}`,
                    mt: 8,
                    py: [3, 6],
                }}
            >
                <Grid container spacing={4} justifyContent="space-evenly">
                    {footers.map((footer) => (
                        <Grid item xs={6} sm={3} key={footer.title}>
                            <Typography variant="h6" color="text.primary" gutterBottom>
                                {footer.title}
                            </Typography>
                            <ul>
                                {footer.description.map((item) => (
                                    <li key={item}>
                                        <Link variant="subtitle1"
                                              color="text.secondary">
                                            {item}
                                        </Link>
                                    </li>
                                ))}
                            </ul>
                        </Grid>
                    ))}
                </Grid>
                <Copyright sx={{mt: 5}}/>
            </Container>
        </Box>
    );
}