import * as React from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import {TopBar} from "../styledComponents/topBar";

export const Dashboard = (props: { children: NonNullable<React.ReactNode> }) => {
    return (
        <Box sx={{display: 'flex'}}>
            <CssBaseline/>
            <TopBar showMenu={true}/>
            <Box
                component="main"
                sx={{
                    backgroundColor: (theme) =>
                        theme.palette.mode === 'light'
                            ? theme.palette.grey[100]
                            : theme.palette.grey[900],
                    flexGrow: 1,
                    height: '100vh',
                    overflow: 'auto',
                }}
            >
                <Toolbar/>
                {props.children}
            </Box>
        </Box>
    );
}