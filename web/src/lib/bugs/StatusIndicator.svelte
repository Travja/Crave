<script>
    export let status = "unconfirmed";
    let statuses = {
        unconfirmed: "Unconfirmed",
        confirmed: "Confirmed",
        rejected: "Rejected",
        inprogress: "In Progress"
    }
</script>

<div class="status {status}">{statuses[status]}</div>

<style>
    .status {
        color: white;
        display: inline-block;
        background-color: #2e2e2e;
        padding: .4em;
        margin-right: 10px;
        border-radius: 8px;
    }

    .confirmed {
        background-color: #49bd4e;
    }

    .rejected {
        background-color: #b43333;
    }

    .inprogress {
        background-color: #4444ff;
    }
</style>