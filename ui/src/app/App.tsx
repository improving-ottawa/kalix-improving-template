import React from 'react';
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import {Provider} from "react-redux";
import {createTheme, ThemeProvider} from "@mui/material";
import {store} from "./redux/store";
import {AdapterDayjs} from '@mui/x-date-pickers/AdapterDayjs';
import {LocalizationProvider} from '@mui/x-date-pickers';
import SignIn from "./screens/login/SignIn";
import SignUp from "./screens/login/SignUp";
import OrdersPage from "./screens/dashboard/orders/OrdersPage";
import CustomersPage from "./screens/dashboard/customers/CustomersPage";
import Pricing from "./screens/productsFlow/pricing/Pricing";

const theme = createTheme({
    palette: {
        primary: {
            main: '#00488A',
            contrastText: '#FFFFFF',
        },
        secondary: {
            main: "#FCFEFF",
            light: "#FCFEFF",
            dark: "#FCFEFF",
            contrastText: "#FCFEFF",
        },
    },
});

function App() {
    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <ThemeProvider theme={theme}>
                <Provider store={store}>
                    <BrowserRouter>
                        <Routes>
                            <Route path="/">
                                <Route index element={<SignIn/>}/>
                                <Route path="sign-up">
                                    <Route index element={<SignUp/>}/>
                                </Route>
                                <Route path="pricing">
                                    <Route index element={<Pricing/>}/>
                                </Route>
                                <Route path="dashboard">
                                    <Route index element={<Navigate to="orders"/>}/>
                                    <Route path="orders">
                                        <Route index element={<OrdersPage/>}/>
                                    </Route>
                                    <Route path="customers">
                                        <Route index element={<CustomersPage/>}/>
                                    </Route>
                                </Route>
                            </Route>
                        </Routes>
                    </BrowserRouter>
                </Provider>
            </ThemeProvider>
        </LocalizationProvider>
    );
}

export default App;
