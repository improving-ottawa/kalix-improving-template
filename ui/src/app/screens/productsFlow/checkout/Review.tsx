import * as React from 'react';
import Typography from '@mui/material/Typography';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Grid from '@mui/material/Grid';
import {selectPurchasingState} from "../../../redux/slices/purchasingSlice";
import {useAppSelector} from "../../../redux/hooks";
import {printAddress} from "../../../utils";
import {Box} from "@mui/material";

export default function Review() {
    const state = useAppSelector(selectPurchasingState)

    const products = [
        ...state.products,
        {name: 'Shipping', price: 0},
    ];
    return (
        <React.Fragment>
            <Typography variant="h6" gutterBottom>
                Order summary
            </Typography>
            <List disablePadding>
                {products.map((product) => (
                    <ListItem key={product.name} sx={{py: 1, px: 0}}>
                        <ListItemText primary={product.name}/>
                        <Typography variant="body2">${product.price}</Typography>
                    </ListItem>
                ))}
                <ListItem sx={{py: 1, px: 0}}>
                    <ListItemText primary="Total"/>
                    <Typography variant="subtitle1" sx={{fontWeight: 700}}>
                        ${products.map(_ => _.price).reduce((p1, p2) => p1 + p2)}
                    </Typography>
                </ListItem>
            </List>
            <Grid container spacing={2}>
                <Grid item xs={12} sm={5}>
                    <Typography variant="h6" gutterBottom sx={{mt: 2}}>
                        Shipping
                    </Typography>
                    <Typography
                        gutterBottom>{state.shippingAddress?.firstName} {state.shippingAddress?.lastName}</Typography>
                    <Typography
                        gutterBottom>
                        {state.shippingAddress?.address && printAddress(state.shippingAddress.address)}
                    </Typography>
                </Grid>
                <Grid item container direction="column" xs={12} sm={7}>
                    <Typography variant="h6" gutterBottom sx={{mt: 2}}>
                        Payment details
                    </Typography>
                    <Box>
                        {[{id: "Card Holder", info: state.paymentInfo?.cardHolder},
                            {
                                id: "Card Number", info: state.paymentInfo?.cardNumber ?
                                    state.paymentInfo?.cardNumber.slice(0, 12).split('').map(_ => 'X').join('')
                                    + state.paymentInfo?.cardNumber.slice(12) : ""
                            },
                            {
                                id: "Expiry Date", info: state.paymentInfo?.expiryDate ?
                                    (new Date(state.paymentInfo.expiryDate)).getMonth() +
                                    "/" + (new Date(state.paymentInfo.expiryDate)).getFullYear().toString().slice(-2) : ""
                            },
                            {
                                id: "CVV",
                                info: state.paymentInfo?.cvv ? state.paymentInfo?.cvv.split('').map(_ => 'X').join('') : ""
                            },
                        ].map((info) => (
                            <React.Fragment key={info.id}>
                                <Grid item xs={6}>
                                    <Typography gutterBottom>{info.id}</Typography>
                                </Grid>
                                <Grid item xs={6}>
                                    <Typography gutterBottom>{info.info}</Typography>
                                </Grid>
                            </React.Fragment>
                        ))}
                    </Box>
                </Grid>
            </Grid>
        </React.Fragment>
    );
}