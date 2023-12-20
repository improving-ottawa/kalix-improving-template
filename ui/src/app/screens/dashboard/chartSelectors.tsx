import {FormControl, InputLabel, MenuItem, Select} from "@mui/material";
import {DateRanges} from "./types";
import Grid from "@mui/material/Grid";
import * as React from "react";
import {Dayjs} from "dayjs";

interface ChartSelectorsProps {
    chartTypes: string[]
    selectedDateRange: { key: string, value: Dayjs }
    setSelectedDateRange: (date: { key: string, value: Dayjs }) => void
    selectedChart: string
    setSelectedChart: (chart: string) => void
}

export const ChartSelectors = (props: ChartSelectorsProps) => {
    return <React.Fragment>
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
                        value={props.selectedChart}
                        labelId="selectChartField"
                        onChange={(e) => {
                            props.setSelectedChart(e.target.value)
                        }}
                        id="changeSelectedChart" label="Chart Type" variant="outlined"
                >
                    {props.chartTypes.map(chart =>
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
                        value={props.selectedDateRange.key}
                        labelId="selectDateRangeField"
                        onChange={(e) => {
                            props.setSelectedDateRange({
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
    </React.Fragment>
}