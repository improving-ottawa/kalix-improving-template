import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import {Copyright} from "../../styledComponents/copyright";
import Container from "@mui/material/Container";
import * as React from "react";
import {Dashboard} from "../Dashboard";
import {ActiveCustomers, LifetimeValue, RecentCustomers} from "./Customers";
import {Typography} from "@mui/material";
import {useState} from "react";
import {CustomersChartTypes, DateRanges} from "../types";
import {Dayjs} from "dayjs";
import {ChartSelectors} from "../chartSelectors";

export default function CustomersPage() {
    const [selectedChart, setSelectedChart] = useState<string>(CustomersChartTypes[0])
    const [selectedDateRange, setSelectedDateRange] = useState<{ key: string, value: Dayjs }>(
        {key: DateRanges.keys().next().value, value: DateRanges.values().next().value}
    )

    return <Dashboard>
        <Container maxWidth="lg" sx={{mt: 4, mb: 4}}>
            <Grid container spacing={3}>
                <Grid item md={6}>
                    <Typography component="h1" variant="h5">
                        Customers
                    </Typography>
                </Grid>
                <ChartSelectors chartTypes={CustomersChartTypes} selectedChart={selectedChart}
                                setSelectedChart={setSelectedChart}
                                selectedDateRange={selectedDateRange} setSelectedDateRange={setSelectedDateRange}/>
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