import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import Chart from "./Chart";
import RecentStats from "./RecentStats";
import RecentOrders from "./RecentOrders";
import {Copyright} from "../../styledComponents/copyright";
import Container from "@mui/material/Container";
import * as React from "react";
import {Dashboard} from "../Dashboard";
import {FormControl, InputLabel, MenuItem, Select, Typography} from "@mui/material";
import {useState} from "react";
import {Dayjs} from "dayjs";
import {DateRanges, OrdersChartTypes} from "../types";

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
                <Grid item md={3} sx={{justifyItems: 'flex-end', flexDirection: "row"}}>
                    <FormControl required
                                 style={{
                                     marginTop: "8px",
                                     marginBottom: "4px",
                                     justifyContent: 'flex-end',
                                     alignItems: "flex-end",
                                     width: '100%'
                                 }}>
                        <InputLabel
                            id="selectChart">Chart Type</InputLabel>
                        <Select fullWidth
                                required
                                value={selectedChart}
                                labelId="selectChartField"
                                onChange={(e) => {
                                    setSelectedChart(e.target.value)
                                }}
                                id="changeSelectedChart" label="Chart Type" variant="outlined"
                        >
                            {OrdersChartTypes.map(chart =>
                                <MenuItem divider value={chart}>{chart}</MenuItem>
                            )}
                        </Select>
                    </FormControl>
                </Grid>
                <Grid item md={3} sx={{justifyItems: 'flex-end', flexDirection: "row"}}>
                    <FormControl required
                                 style={{
                                     marginTop: "8px",
                                     marginBottom: "4px",
                                     justifyContent: 'flex-end',
                                     alignItems: "flex-end",
                                     width: '100%'
                                 }}>
                        <InputLabel
                            id="selectDateRange">Date Range</InputLabel>
                        <Select fullWidth
                                required
                                value={selectedDateRange.key}
                                labelId="selectDateRangeField"
                                onChange={(e) => {
                                    setSelectedDateRange({
                                        key: e.target.value,
                                        value: DateRanges.get(e.target.value) ?? Array.from(DateRanges.values())[0]
                                    })
                                }}
                                id="changeDateRange" label="Date Range" variant="outlined"
                        >
                            {Array.from(DateRanges.keys()).map(chart =>
                                <MenuItem divider value={chart}>{chart}</MenuItem>
                            )}
                        </Select>
                    </FormControl>
                </Grid>
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