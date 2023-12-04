import * as React from 'react';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import LanguageIcon from '@mui/icons-material/Language';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import PeopleIcon from '@mui/icons-material/People';
import LayersIcon from '@mui/icons-material/Layers';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';

export const mainNavItems = (
    <React.Fragment>
        <ListItemButton>
            <ListItemIcon>
                <ShoppingCartIcon/>
            </ListItemIcon>
            <ListItemText primary="Recent Orders"/>
        </ListItemButton>
        <ListItemButton>
            <ListItemIcon>
                <PeopleIcon/>
            </ListItemIcon>
            <ListItemText primary="Customers"/>
        </ListItemButton>
        <ListItemButton>
            <ListItemIcon>
                <LanguageIcon/>
            </ListItemIcon>
            <ListItemText primary="Sessions"/>
        </ListItemButton>
        <ListItemButton>
            <ListItemIcon>
                <AssignmentTurnedInIcon/>
            </ListItemIcon>
            <ListItemText primary="Inventory"/>
        </ListItemButton>
        <ListItemButton>
            <ListItemIcon>
                <AttachMoneyIcon/>
            </ListItemIcon>
            <ListItemText primary="Conversions"/>
        </ListItemButton>
    </React.Fragment>
);

export const secondaryNavItems = (
    <React.Fragment>
        <ListItemButton>
            <ListItemText primary="Kalix Console"/>
        </ListItemButton>
    </React.Fragment>
);