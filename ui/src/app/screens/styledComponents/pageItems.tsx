import {Paper, styled} from "@mui/material";

export const FullPageItem = styled(Paper)(({ theme }) => ({
    border: 0,
    paddingTop: 16,
    paddingLeft: 180,
    paddingRight: 180,
    backgroundColor: 'transparent',
    minHeight: '100vh',
    alignItems: 'center',
    justifyItems: 'center',
}));

export const LeftAlignedItem = styled(Paper)(({ theme }) => ({
    border: 0,
    textAlign: 'left',
    alignItems: 'left',
    paddingTop: 16,
    paddingLeft: 24,
}));

export const RightAlignedItem = styled(Paper)(({ theme }) => ({
    border: 0,
    textAlign: 'right',
    paddingTop: 16,
    paddingLeft: 24,
    alignItems: "flex-start"
}));

export const RightTopAlignedItem = styled(Paper)(({ theme }) => ({
    border: 0,
    textAlign: 'right',
    alignItems: 'right',
    justifyItems: 'top'
}));


export const TopAlignedItem = styled(Paper)(({ theme }) => ({
    border: 0,
    paddingTop: 16,
    paddingLeft: 180,
    paddingRight: 180,
    backgroundColor: 'transparent',
    width: 'inherit'
}));

export const CenteredItem = styled(Paper)(({ theme }) => ({
    border: 0,
    textAlign: 'center',
    justifyItems: 'center',
    paddingTop: 16,
    paddingLeft: 180,
    paddingRight: 180
}));