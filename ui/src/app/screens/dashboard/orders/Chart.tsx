import * as React from 'react';
import {useTheme} from '@mui/material/styles';
import {BarChart, XAxis, YAxis, ResponsiveContainer, Tooltip, Bar, Rectangle} from 'recharts';
import CardTitle from "../components";
import {orderData} from "../fakeData";
import * as _ from "lodash"

export default function Chart() {
    const theme = useTheme();

    const averageByDate: { day: string; avg: number }[] = _.chain(orderData)
        .groupBy(order => order.date)
        .map((entries, day) => {
            return {day, avg: _.meanBy(entries, entry => entry.amount)}
        })
        .sortBy(avgDay => avgDay.day)
        .value()

    const maxAvg = _.maxBy(averageByDate, item => item.avg)?.avg
    const roundedAvg = maxAvg ? Math.round(maxAvg / 50) * 50 : maxAvg

    return <React.Fragment>
        <CardTitle>Today</CardTitle>
        <ResponsiveContainer>
            <BarChart
                data={averageByDate}
                margin={{
                    top: 16,
                    right: 16,
                    bottom: 0,
                    left: 24,
                }}
            >
                <XAxis
                    dataKey="day"
                    style={theme.typography.body2}
                />
                <YAxis
                    max={roundedAvg}
                    tickMargin={roundedAvg ? roundedAvg / 500 : 50}
                    style={theme.typography.body2}
                    tickCount={10}
                />
                <Tooltip/>
                <Bar dataKey="avg" fill="#005596" activeBar={<Rectangle fill="blue" stroke="blue"/>}/>
            </BarChart>
        </ResponsiveContainer>
    </React.Fragment>;
}