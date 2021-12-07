<script>

</script>

<div class="content">
    <h1>Enter your Walmart Receipt Information</h1>

    <form id="walmartForm">
        <label>Store ID: <input type="text" placeholder="03589"/></label>
        <label>Purchase Date: <input type="date"/></label>
        <label>
            Card Type:
            <select id="card-type" aria-required="true">
                <option value="visa">Visa</option>
                <option value="mastercard">Mastercard</option>
                <option value="amex">Amex</option>
                <option value="discover">Discover</option>
                <option value="debit">Debit</option>
                <option value="other">Other</option>
            </select>
        </label>
        <label>Last Four of Card: <input type="text" placeholder="3589"/></label>
        <label>Receipt Total: <input type="number" min="0" max="10000" step="0.01" placeholder="5.00"/></label>
        <div class="button submit">Submit</div>
    </form>
</div>

<style>

    .content {
        flex-grow: 1;
        padding: 0.7rem;
    }

    .submit {
        background: var(--accent-color);
    }

    @media only screen and (min-width: 768px) {
        .content {
            padding: 1rem;
        }
    }

</style>