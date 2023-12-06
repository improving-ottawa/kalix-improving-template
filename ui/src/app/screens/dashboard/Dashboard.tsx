import * as React from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import {SideNav, TopNav} from "../styledComponents/navBars";
import {AppBar} from "./components";

export const Dashboard = (props: { children: NonNullable<React.ReactNode> }) => {
    const [open, setOpen] = React.useState(true);
    const toggleDrawer = () => {
        setOpen(!open);
    };

    return (
        <Box sx={{display: 'flex'}}>
            <CssBaseline/>
            <AppBar position="absolute"
                    color="default"
                    open={open}
                    elevation={0}
                    sx={{borderBottom: (theme) => `1px solid ${theme.palette.divider}`}}>
                <TopNav forDashboard={true} open={open} toggleDrawer={toggleDrawer}/>
            </AppBar>
            <SideNav open={open} toggleDrawer={toggleDrawer}/>
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