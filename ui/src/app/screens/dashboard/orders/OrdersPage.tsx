import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import Chart from "./Chart";
import RecentStats from "./RecentStats";
import RecentOrders from "./RecentOrders";
import {Copyright} from "../../styledComponents/copyright";
import Container from "@mui/material/Container";
import * as React from "react";
import {Dashboard} from "../Dashboard";
import {Typography} from "@mui/material";
import {useState} from "react";
import {Dayjs} from "dayjs";
import {DateRanges, OrdersChartTypes} from "../types";
import {ChartSelectors} from "../chartSelectors";

export default function OrdersPage() {
    const [selectedChart, setSelectedChart] = useState<string>(OrdersChartTypes[0])
    const [selectedDateRange, setSelectedDateRange] = useState<{ key: string, value: Dayjs }>(
        {key: DateRanges.keys().next().value, value: DateRanges.values().next().value}
    )

    return <Dashboard>
        <Container maxWidth="lg" sx={{mt: 4, mb: 4}}>
            <Grid container spacing={3}>
                <Grid item md={6}>
                    <Typography component="h1" variant="h5">
                        Orders
                    </Typography>
                </Grid>
                <ChartSelectors chartTypes={OrdersChartTypes} selectedChart={selectedChart}
                                setSelectedChart={setSelectedChart}
                                selectedDateRange={selectedDateRange} setSelectedDateRange={setSelectedDateRange}/>
                <Grid item xs={12} md={8} lg={9}>
                    <Paper
                        sx={{
                            p: 2,
                            display: 'flex',
                            flexDirection: 'column',
                            height: 240,
                        }}
                    >
                        <Chart/>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={4} lg={3}>
                    <Paper
                        sx={{
                            p: 2,
                            display: 'flex',
                            flexDirection: 'column',
                            height: 240,
                        }}
                    >
                        <RecentStats/>
                    </Paper>
                </Grid>
                <Grid item xs={12}>
                    <Paper sx={{p: 2, display: 'flex', flexDirection: 'column'}}>
                        <RecentOrders/>
                    </Paper>
                </Grid>
            </Grid>
            <Copyright sx={{pt: 4}}/>
        </Container>
    </Dashboard>
}