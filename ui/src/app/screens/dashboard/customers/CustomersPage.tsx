import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import {Copyright} from "../../styledComponents/copyright";
import Container from "@mui/material/Container";
import * as React from "react";
import {Dashboard} from "../Dashboard";
import {ActiveCustomers, LifetimeValue, RecentCustomers} from "./Customers";

export default function CustomersPage() {
    return <Dashboard>
        <Container maxWidth="lg" sx={{mt: 4, mb: 4}}>
            <Grid container spacing={3}>
                <Grid item xs={12} md={6} lg={6}>
                    <Paper
                        sx={{
                            p: 2,
                            display: 'flex',
                            flexDirection: 'column',
                            height: 'auto',
                        }}
                    >
                        <RecentCustomers/>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={6} lg={6}>
                    <Paper
                        sx={{
                            p: 2,
                            display: 'flex',
                            flexDirection: 'column',
                            height: 'auto',
                        }}
                    >
                        <ActiveCustomers/>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={6} lg={6}>
                    <Paper
                        sx={{
                            p: 2,
                            display: 'flex',
                            flexDirection: 'column',
                            height: 'auto',
                        }}
                    >
                        <LifetimeValue/>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={6} lg={6}>
                    <Paper
                        sx={{
                            p: 2,
                            display: 'flex',
                            flexDirection: 'column',
                            height: 'auto',
                        }}
                    >
                    </Paper>
                </Grid>
            </Grid>
            <Copyright sx={{pt: 4}}/>
        </Container>
    </Dashboard>
}