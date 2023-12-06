import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Link from "@mui/material/Link";
import {IconButton} from "@mui/material";
import {AccountCircleTwoTone} from "@mui/icons-material";
import * as React from "react";
import {useNavigate} from "react-router-dom";
import MenuIcon from "@mui/icons-material/Menu";
import Badge from "@mui/material/Badge";
import NotificationsIcon from "@mui/icons-material/Notifications";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import Divider from "@mui/material/Divider";
import {mainNavItems, secondaryNavItems} from "../dashboard/navItems";
import {Drawer} from "../dashboard/components";
import List from "@mui/material/List";

export const TopNav = (props: { forDashboard?: boolean, open?: boolean, toggleDrawer?: () => void }) => {
    const navigate = useNavigate()

    return <Toolbar sx={props.forDashboard ? {pr: '24px'} : {flexWrap: 'wrap'}}>
        {props.forDashboard && <IconButton
            edge="start"
            color="inherit"
            aria-label="open drawer"
            onClick={props.toggleDrawer}
            sx={{
                marginRight: '36px',
                ...(props.open && {display: 'none'}),
            }}
        >
            <MenuIcon/>
        </IconButton>}
        <Typography component="h1" variant="h6" color="inherit" noWrap sx={{flexGrow: 1}}>
            Logoipsum
        </Typography>
        <Link
            variant="button"
            color="text.primary"
            onClick={() => navigate("/pricing")}
            sx={{my: 1, mx: 1.5}}
        >
            Pricing
        </Link>
        <Link
            variant="button"
            color="text.primary"
            href="#"
            sx={{my: 1, mx: 1.5}}
        >
            About Us
        </Link>
        <Link
            variant="button"
            color="text.primary"
            href="#"
            sx={{my: 1, mx: 1.5}}
        >
            Resources
        </Link>
        {props.forDashboard ?
            <IconButton color="inherit">
                <Badge badgeContent={4} color="secondary">
                    <NotificationsIcon/>
                </Badge>
            </IconButton> : <IconButton onClick={() => navigate("/dashboard")} sx={{my: 1, mx: 1.5}}>
                <AccountCircleTwoTone/>
            </IconButton>}
    </Toolbar>
}

export const SideNav = (props: { open: boolean, toggleDrawer: () => void }) => {
    return <Drawer variant="permanent" open={props.open}>
        <Toolbar
            sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'flex-end',
                px: [1],
            }}
        >
            <IconButton onClick={props.toggleDrawer}>
                <ChevronLeftIcon/>
            </IconButton>
        </Toolbar>
        <Divider/>
        <List component="nav">
            {mainNavItems(useNavigate())}
            <Divider sx={{my: 1}}/>
            {secondaryNavItems}
        </List>
    </Drawer>
}