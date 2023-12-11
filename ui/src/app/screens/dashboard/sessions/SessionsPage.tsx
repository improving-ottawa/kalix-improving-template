import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import {Copyright} from "../../styledComponents/copyright";
import Container from "@mui/material/Container";
import * as React from "react";
import {Dashboard} from "../Dashboard";
import SessionsBarChart from "./SessionsBarChart";

export default function SessionsPage() {
    return <Dashboard>
        <Container maxWidth="lg" sx={{mt: 4, mb: 4}}>
            <Grid container spacing={3}>
                <SessionsBarChart/>
                <Grid item md={12}>
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