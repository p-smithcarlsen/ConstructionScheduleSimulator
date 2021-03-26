using Microsoft.EntityFrameworkCore.Migrations;

namespace Database.EntityFrameworkCore.Migrations
{
    public partial class Test2 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "Tasks",
                columns: table => new
                {
                    TaskID = table.Column<string>(nullable: false),
                    EstimatedResources = table.Column<int>(nullable: false),
                    ActualResources = table.Column<int>(nullable: false),
                    Zone = table.Column<string>(nullable: true),
                    Craft = table.Column<string>(nullable: true),
                    Operation = table.Column<string>(nullable: true),
                    ActualDuration = table.Column<double>(nullable: true),
                    EstimatedDuration = table.Column<double>(nullable: false),
                    Progress = table.Column<int>(nullable: false),
                    ParentID = table.Column<string>(nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Tasks", x => x.TaskID);
                    table.ForeignKey(
                        name: "FK_Tasks_Tasks_ParentID",
                        column: x => x.ParentID,
                        principalTable: "Tasks",
                        principalColumn: "TaskID",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateIndex(
                name: "IX_Tasks_ParentID",
                table: "Tasks",
                column: "ParentID");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "Tasks");
        }
    }
}
