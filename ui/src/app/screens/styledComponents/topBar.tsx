import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Link from "@mui/material/Link";
import {IconButton} from "@mui/material";
import {AccountCircleTwoTone} from "@mui/icons-material";
import AppBar from "@mui/material/AppBar";
import * as React from "react";
import {useNavigate} from "react-router-dom";
import MenuIcon from "@mui/icons-material/Menu";
import Badge from "@mui/material/Badge";
import NotificationsIcon from "@mui/icons-material/Notifications";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import Divider from "@mui/material/Divider";
import List from "@mui/material/List";
import {mainNavItems, secondaryNavItems} from "../dashboard/navItems";
import {Drawer} from "../dashboard/components";

export const TopBar = (props: { showMenu?: boolean } = {showMenu: false}) => {
    const navigate = useNavigate()

    const [open, setOpen] = React.useState(true);
    const toggleDrawer = () => {
        setOpen(!open);
    };

    return <AppBar
        position="static"
        color="default"
        elevation={0}
        sx={{borderBottom: (theme) => `1px solid ${theme.palette.divider}`}}
    >
        <Toolbar sx={{flexWrap: 'wrap'}}>
            {props.showMenu && <IconButton
                edge="start"
                color="inherit"
                aria-label="open drawer"
                onClick={toggleDrawer}
                sx={{
                    marginRight: '36px',
                    ...(open && {display: 'none'}),
                }}
            >
                <MenuIcon/>
            </IconButton>}
            <Typography variant="h6" color="inherit" noWrap sx={{flexGrow: 1}}>
                Logoipsum
            </Typography>
            <nav>
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
            </nav>
            <Toolbar
                sx={{
                    pr: '24px',
                }}
            >
                {props.showMenu ?
                    <IconButton color="inherit">
                        <Badge badgeContent={4} color="secondary">
                            <NotificationsIcon/>
                        </Badge>
                    </IconButton> : <IconButton onClick={() => navigate("/dashboard")} sx={{my: 1, mx: 1.5}}>
                        <AccountCircleTwoTone/>
                    </IconButton>}
            </Toolbar>
        </Toolbar>
        {props.showMenu && <Drawer variant="permanent" open={open}>
            <Toolbar
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'flex-end',
                    px: [1],
                }}
            >
                <IconButton onClick={toggleDrawer}>
                    <ChevronLeftIcon/>
                </IconButton>
            </Toolbar>
            <Divider/>
            <List component="nav">
                {mainNavItems}
                <Divider sx={{my: 1}}/>
                {secondaryNavItems}
            </List>
        </Drawer>}
    </AppBar>
}