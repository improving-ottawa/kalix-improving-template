import * as React from 'react';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import PeopleIcon from '@mui/icons-material/People';
import {NavigateFunction} from "react-router-dom";

export const mainNavItems = (navigate: NavigateFunction) => {
    return <React.Fragment>
        <ListItemButton onClick={() => navigate("/dashboard/orders")}>
            <ListItemIcon>
                <ShoppingCartIcon/>
            </ListItemIcon>
            <ListItemText primary="Orders"/>
        </ListItemButton>
        <ListItemButton onClick={() => navigate("/dashboard/customers")}>
            <ListItemIcon>
                <PeopleIcon/>
            </ListItemIcon>
            <ListItemText primary="Customers"/>
        </ListItemButton>
        {
            //<ListItemButton onClick={() => navigate("/dashboard/sessions")}>
            //    <ListItemIcon>
            //        <LanguageIcon/>
            //    </ListItemIcon>
            //    <ListItemText primary="Sessions"/>
            //</ListItemButton>
            //<ListItemButton>
            //    <ListItemIcon>
            //        <AssignmentTurnedInIcon/>
            //    </ListItemIcon>
            //    <ListItemText primary="Inventory"/>
            //</ListItemButton>
            //<ListItemButton>
            //    <ListItemIcon>
            //        <InsightsIcon/>
            //    </ListItemIcon>
            //    <ListItemText primary="Conversions"/>
            //</ListItemButton>
        }
    </React.Fragment>
}

export const secondaryNavItems = (
    <ListItemButton>
        <ListItemText primary="Kalix Console"/>
    </ListItemButton>
);