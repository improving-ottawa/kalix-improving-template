import React from "react";
import {BarChart} from "@mui/x-charts";
import {orderData} from "../fakeData";
import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import {FormControl} from "@mui/material";
import Select, {SelectChangeEvent} from '@mui/material/Select';

const pData = [2400, 1398, 9800, 3908, 4800];
const xLabels = orderData.map(_ => _.name)

export default function SessionsBarChart() {
    const [chartType, setChartType] = React.useState('Sessions Over Time');

    const handleChange = (event: SelectChangeEvent) => {
        setChartType(event.target.value as string);
    };

    return <Grid container spacing={2}>
        <FormControl fullWidth>
            <Select
                labelId="demo-simple-select-label"
                id="demo-simple-select"
                value={chartType}
                label="Sessions Bar Charts"
                onChange={handleChange}
            >
            </Select>
        </FormControl>
        <Grid item md={12}>
            <Paper
                sx={{
                    p: 2,
                    display: 'flex',
                    flexDirection: 'column',
                    height: 'auto',
                }}
            >
                <BarChart
                    width={500}
                    height={300}
                    series={[
                        {data: pData, id: 'total time'},
                    ]}
                    xAxis={[{data: xLabels, scaleType: 'band'}]}
                />
            </Paper>
        </Grid>
    </Grid>
}