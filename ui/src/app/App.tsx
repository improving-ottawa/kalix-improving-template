import React from 'react';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {Provider} from "react-redux";
import {createTheme, ThemeProvider} from "@mui/material";
import {store} from "./redux/store";
import {Login} from "@mui/icons-material";
import {AdapterDayjs} from '@mui/x-date-pickers/AdapterDayjs';
import {LocalizationProvider} from '@mui/x-date-pickers';

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
                                <Route index element={<Login/>}/>
                            </Route>
                        </Routes>
                    </BrowserRouter>
                </Provider>
            </ThemeProvider>
        </LocalizationProvider>
    );
}

export default App;
