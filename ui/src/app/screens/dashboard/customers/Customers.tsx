import * as React from 'react';
import Link from '@mui/material/Link';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import {FakeOrder, orderData} from "../fakeData";
import CardTitle from "../components";

function preventDefault(event: React.MouseEvent) {
    event.preventDefault();
}

const CustomersTable = (props: { data: FakeOrder[] }) => <Table size="small">
    <TableHead>
        <TableRow>
            <TableCell>Date</TableCell>
            <TableCell>Name</TableCell>
            <TableCell align="right">Sale Amount</TableCell>
        </TableRow>
    </TableHead>
    <TableBody>
        {props.data.map((row) => (
            <TableRow key={row.id}>
                <TableCell>{row.date}</TableCell>
                <TableCell>{row.name}</TableCell>
                <TableCell align="right">{`$${row.amount}`}</TableCell>
            </TableRow>
        ))}
    </TableBody>
</Table>

export function RecentCustomers() {
    return (
        <React.Fragment>
            <CardTitle>Recent Customers</CardTitle>
            <Table size="small">
                <CustomersTable data={orderData}/>
            </Table>
            <Link color="primary" onClick={preventDefault} sx={{mt: 3}}>
                See more customers
            </Link>
        </React.Fragment>
    );
}

export function ActiveCustomers() {
    return (
        <React.Fragment>
            <CardTitle>Active Customers</CardTitle>
            <Table size="small">
                <CustomersTable data={orderData}/>
            </Table>
            <Link color="primary" onClick={preventDefault} sx={{mt: 3}}>
                See more customers
            </Link>
        </React.Fragment>
    );
}

export function LifetimeValue() {
    return (
        <React.Fragment>
            <CardTitle>Lifetime Value</CardTitle>
            <Table size="small">
                <CustomersTable data={orderData}/>
            </Table>
            <Link color="primary" onClick={preventDefault} sx={{mt: 3}}>
                See more customers
            </Link>
        </React.Fragment>
    );
}