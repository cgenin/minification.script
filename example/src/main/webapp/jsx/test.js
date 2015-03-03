/** @jsx React.DOM */
var TitleBox = React.createClass({
    render: function() {
        return (
            <h4  className="inline">Toolbar => </h4>
        );
    }
});
React.render(
    <TitleBox />,
    document.getElementById('react-title')
);